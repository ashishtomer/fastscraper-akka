package com.fastscraping.actor

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, ActorSystem, Behavior}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{ContentTypes, HttpEntity}
import akka.http.scaladsl.server.Directives._
import com.fastscraping.actor.message.{LinkManagerActorMessage, ScrapeJob}
import com.fastscraping.model._
import com.fastscraping.pagenavigation.scrape.ScrapeCrawlLinks.ScrapeLinksBy
import com.fastscraping.pagenavigation.scrape.{ScrapeCrawlLinks, ScrapeLinks, ScrapeWithSelector}
import com.fastscraping.pagenavigation.selenium.ElementFinder.FindElementBy

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
            PageUniqueness("https://www\\.amazon\\.in/b\\?ie=UTF8&node=6308595031",
              UniqueTag("div.acs_widget-title.acs_widget-title__secondary", None, None),
              UniqueString("EXPLORE INDIA'S LARGEST ONLINE STORE")
            ),
            Seq(
              PageWork(ScrapeCrawlLinks(ScrapeLinksBy.BY_TEXT.toString, "Toys & Games"))
            )
          ),
          WebpageIdentifier(
            PageUniqueness(
              "https://www\\.amazon\\.in/.+",
              UniqueTag("span.a-size-base.a-color-base", Some("Toys & Games"), Some(Element(FindElementBy.BY_ID.toString, "#departments"))),
              UniqueString("Toys & Games", Some(Element(FindElementBy.BY_ID.toString, "#departments")))
            ),
            Seq(
              PageWork(ScrapeCrawlLinks(FindElementBy.BY_CSS_SELECTOR.toString, "a.a-link-normal.a-text-normal")),
              PageWork(ScrapeCrawlLinks(FindElementBy.BY_CSS_SELECTOR.toString, "li.a-normal a"), Some(Element(FindElementBy.BY_CSS_SELECTOR.toString, "div.a-text-center[role=\"navigation\"]")))
            )
          ),
          WebpageIdentifier(
            PageUniqueness(
              "https://www\\.amazon\\.in/.+",
              UniqueTag("span.nav-a-content", Some("Toys & Games"), Some(Element(FindElementBy.BY_ID.toString, "#nav-progressive-subnav"))),
              UniqueString("Toys & Games", Some(Element(FindElementBy.BY_ID.toString, "#nav-progressive-subnav")))
              ),
            Seq(
              PageWork(ScrapeWithSelector("span#productTitle", DataToExtract("product_name", ScrapeDataTypes.TEXT), "toys"))
            )
          )
        )

        linkManager ! ScrapeJob("https://www.amazon.in/b?ie=UTF8&node=6308595031", idfs)
        complete(HttpEntity(ContentTypes.`application/json`, "{\"message\":\"Scraping started\"}"))
      }
    }

    val bindingFuture = Http().newServerAt("0.0.0.0", 8082).bind(route)

  }

}
