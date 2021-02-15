package com.fastscraping.actor

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, ActorSystem, Behavior}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{ContentTypes, HttpEntity}
import akka.http.scaladsl.server.Directives._
import com.fastscraping.actor.message.{LinkManagerActorMessage, ScrapeJob}
import com.fastscraping.model.{Element, PageWork, UniqueTag, WebpageIdentifier}
import com.fastscraping.pagenavigation.action.FindElementActions
import com.fastscraping.pagenavigation.scrape.{ScrapeCrawlLinks, ScrapeLinks}

object HttpListeningActor {

  def apply(): Behavior[Nothing] = Behaviors.setup[Nothing] { context =>
    val linkManagerActor: ActorRef[LinkManagerActorMessage] = context.spawn(LinkManagerActor(), "link-manager")

    listenHttpCalls(linkManagerActor)(context.system)

    Behaviors.empty[Nothing]
  }

  private def listenHttpCalls(linkManager: ActorRef[LinkManagerActorMessage])(implicit as: ActorSystem[Nothing]): Unit = {
    val route = path("scrape") {
      get {
        val idfs = Seq(
          WebpageIdentifier(
            urlRegex = "https://en\\.wikipedia\\.org/wiki/Main_Page",
            uniqueStringOnPage = Some("Welcome to Wikipedia"),
            UniqueTag("div#mw-content-text", None),

            Seq(
              PageWork(ScrapeCrawlLinks("BY_TEXT", "All portals"), Some(Element("BY_ID", "mp-topbanner")))
            )
          ),
          WebpageIdentifier(
            urlRegex = "https://en\\.wikipedia\\.org/wiki/Wikipedia:Contents/Portals",
            uniqueStringOnPage = Some("Wikipedia:Contents/Portals"),
            UniqueTag("h1#firstHeading", None),

            Seq(
              PageWork(ScrapeLinks(".*wikipedia\\.org/wiki/.*", "scraped_links")),
              PageWork(ScrapeCrawlLinks("BY_REGEX", "https://en\\.wikipedia\\.org/wiki/Portal:.*"))
            )
          )
        )

        linkManager ! ScrapeJob("https://en.wikipedia.org/wiki/Main_Page", idfs, "wikipedia")
        complete(HttpEntity(ContentTypes.`application/json`, "{\"message\":\"Scraping started\"}"))
      }
    }

    val bindingFuture = Http().newServerAt("0.0.0.0", 8082).bind(route)

  }

}
