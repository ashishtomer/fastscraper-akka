package com.fastscraping.actor

import java.util.concurrent.TimeUnit

import akka.actor.typed.{Behavior, Terminated}
import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import com.fastscraping.actor.message.{ScrapeWebpage, WorkerActorMessage}
import com.fastscraping.model.WebpageIdentifier
import com.fastscraping.pagenavigation.selenium.{PageReader, ScrapeJobExecutor}
import org.openqa.selenium.firefox.FirefoxDriver
import org.openqa.selenium.remote.RemoteWebDriver

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
  private val driver:RemoteWebDriver = new FirefoxDriver()
  driver.manage().timeouts().pageLoadTimeout(60, TimeUnit.SECONDS).implicitlyWait(5, TimeUnit.SECONDS)

  private val pageReader = PageReader(driver)
  private val scrapeJobExecutor = ScrapeJobExecutor(pageReader)

  override def onMessage(msg: WorkerActorMessage): Behavior[WorkerActorMessage] = startScraping(msg)

  def startScraping(msg: WorkerActorMessage) = msg match {
    case scrapeWebpage@ScrapeWebpage(link, jobId, webpageIdentifiers) =>
      pageReader.get(link)
      scrapeJobExecutor.execute(webpageIdentifiers)

      Behaviors.same[WorkerActorMessage]
  }
}

object WorkerActor {
  def apply() = Behaviors.setup[WorkerActorMessage](context => new WorkerActor(context))

}
