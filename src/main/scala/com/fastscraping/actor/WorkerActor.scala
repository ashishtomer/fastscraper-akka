package com.fastscraping.actor

import java.util.concurrent.TimeUnit

import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import akka.actor.typed.{ActorRef, Behavior}
import com.fastscraping.actor.message.{ScrapeNextPage, StartScraping, WorkerActorMessage}
import com.fastscraping.data.bson.CrawlLink
import com.fastscraping.data.{FsMongoDB, MongoProvider}
import com.fastscraping.model.WebpageIdentifier
import com.fastscraping.pagenavigation.selenium.{PageReader, ScrapeJobExecutor}
import com.fastscraping.utils.IncorrectScrapeJob
import org.openqa.selenium.firefox.FirefoxDriver
import org.openqa.selenium.remote.RemoteWebDriver

import scala.collection.mutable
import scala.collection.mutable.ListBuffer
import scala.concurrent.ExecutionContext
import scala.util.control.NonFatal
import scala.util.{Failure, Success, Try}

/**
 * This is an actor which will do actual work on a web page.
 * Usually there will0 be multiple instances of this actor
 * running in parallel. When this actor is spawned, it starts a driver.
 *
 * LinkManagerActor will create the router of this actor and supply the link to the actor
 *
 * There will be sync between instances of this actor and LinkManagerActor.
 * If this actor get too many messages in its queue, then it'll inform LinkManagerActor
 * This will avoid OutOfMemory error.
 *
 * This actor's main job is to receive the URL, open the URL in browser,
 * perform action and gather data from the page.
 *
 * ######### This actor reuses it browser rather than opening a browser for each message #########
 *
 * @param context
 */
class WorkerActor(context: ActorContext[WorkerActorMessage]) extends AbstractBehavior[WorkerActorMessage](context) {
  implicit val ec: ExecutionContext = context.executionContext
  val self: ActorRef[WorkerActorMessage] = context.self

  val linkQueueLimit = 100
  val linkQueue: mutable.Queue[String] = mutable.Queue()
  private implicit lazy val pageReader: PageReader = PageReader(driver)
  private lazy val driver: RemoteWebDriver = new FirefoxDriver()
  driver.manage().timeouts().pageLoadTimeout(60, TimeUnit.SECONDS).implicitlyWait(5, TimeUnit.SECONDS)

  override def onMessage(msg: WorkerActorMessage): Behavior[WorkerActorMessage] = startScraping(msg)

  def startScraping(msg: WorkerActorMessage): Behavior[WorkerActorMessage] = msg match {
    case StartScraping(seedUrl, jobId, webpageIdentifiers) =>
      if (webpageIdentifiers.isEmpty) {
        throw IncorrectScrapeJob("No web page identifier found")
      }

      val db = FsMongoDB(MongoProvider.getDatabase(Some(jobId)))
      linkQueue.enqueue(seedUrl)
      val identifiers: ListBuffer[WebpageIdentifier] = ListBuffer(webpageIdentifiers:_*)
      context.self ! ScrapeNextPage(identifiers, Some(jobId), db)
      Behaviors.same[WorkerActorMessage]

    case readNext: ScrapeNextPage =>

      import readNext._

      Try(linkQueue.dequeue()) match {

        case Success(link) =>
          ScrapeJobExecutor()(pageReader, db).execute(link, webpageIdentifiers, jobId)
          self ! ScrapeNextPage(webpageIdentifiers, jobId, db)

        case Failure(_: NoSuchElementException) =>
          db.nextScrapeLinks(jobId, linkQueueLimit) match {

            case links: mutable.Buffer[CrawlLink] if links.nonEmpty =>
              linkQueue.enqueueAll(links.map(_._link_to_crawl))
              self ! ScrapeNextPage(webpageIdentifiers, jobId, db)

            case NonFatal(ex) =>
              println(s"Could not find links to scrape. Stopping scraping. ${ex.getMessage}")
              throw ex

            case _ => println(s"No more links to scrape. Stopping scraping.")
          }
      }

      Behaviors.same[WorkerActorMessage]
  }

}

object WorkerActor {
  def apply() = Behaviors.setup[WorkerActorMessage](context => new WorkerActor(context))
}
