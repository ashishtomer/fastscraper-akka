package com.fastscraping.actor

import java.net.URL
import java.util.concurrent.TimeUnit

import akka.actor.PoisonPill
import akka.actor.typed.{ActorRef, Behavior, Terminated}
import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import com.fastscraping.actor.message.{ScrapeNextPage, StartScraping, WorkerActorMessage}
import com.fastscraping.data.{Database, MongoDb}
import com.fastscraping.model.WebpageIdentifier
import com.fastscraping.pagenavigation.selenium.{PageReader, ScrapeJobExecutor}
import com.fastscraping.utils.IncorrectScrapeJob
import org.openqa.selenium.firefox.FirefoxDriver
import org.openqa.selenium.remote.RemoteWebDriver

import scala.collection.mutable
import scala.concurrent.{ExecutionContext, ExecutionContextExecutor, Future, duration}
import scala.concurrent.duration.FiniteDuration
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
  private lazy val driver:RemoteWebDriver = new FirefoxDriver()
  driver.manage().timeouts().pageLoadTimeout(60, TimeUnit.SECONDS).implicitlyWait(5, TimeUnit.SECONDS)

  override def onMessage(msg: WorkerActorMessage): Behavior[WorkerActorMessage] = startScraping(msg)

  def startScraping(msg: WorkerActorMessage): Behavior[WorkerActorMessage] = msg match {
    case StartScraping(seedUrl, jobId, webpageIdentifiers) =>
      if (webpageIdentifiers.isEmpty) {
        throw IncorrectScrapeJob("No web page identifier found")
      }

      val dbName = new URL(seedUrl).getHost.replaceAll("[\\./]", "_") + jobId
      val db = MongoDb("127.0.0.1", 27017, dbName)
      linkQueue.enqueue(seedUrl)
      context.self ! ScrapeNextPage(webpageIdentifiers, db)
      Behaviors.same[WorkerActorMessage]

    case readNext: ScrapeNextPage =>

      import readNext._

      Try(linkQueue.dequeue()) match {
        case Success(link) =>
          ScrapeJobExecutor()(pageReader, db).execute(link, webpageIdentifiers)
          self ! ScrapeNextPage(webpageIdentifiers, db)

        case Failure(_: NoSuchElementException) =>
          db.nextScrapeLinks(linkQueueLimit)
            .onComplete {
              case Success(links) if links.nonEmpty =>
                linkQueue.enqueueAll(links)
                self ! ScrapeNextPage(webpageIdentifiers, db)
              case Failure(ex) =>
                println(s"Could not find links to scrape. Stopping scraping. ${ex.getMessage}")
                throw ex
              case _ =>
                println(s"No more links to scrape. Stopping scraping.")

            }
      }

      Behaviors.same[WorkerActorMessage]
  }

  private def readNextPages(identifiers: Seq[WebpageIdentifier], db: Database)(implicit ec: ExecutionContext) = {

  }
}

object WorkerActor {
  def apply() = Behaviors.setup[WorkerActorMessage](context => new WorkerActor(context))

}
