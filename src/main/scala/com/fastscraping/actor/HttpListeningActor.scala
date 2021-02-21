package com.fastscraping.actor

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, ActorSystem, Behavior}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{ContentTypes, HttpEntity}
import akka.http.scaladsl.server.Directives._
import com.fastscraping.actor.message.{LinkManagerActorMessage, ScrapeJob}
import com.fastscraping.model._
import com.fastscraping.pagenavigation.scrape.ScrapeCrawlLinks.ScrapeLinksBy
import com.fastscraping.pagenavigation.scrape.{ScrapeCrawlLinks, ScrapeWithSelector}
import com.fastscraping.pagenavigation.selenium.ElementFinder.FindElementBy
import com.fastscraping.utils.FsLogging

object HttpListeningActor extends FsLogging {

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
              Seq(UniqueTag("div.acs_widget-title.acs_widget-title__secondary", None, None)),
              Seq(UniqueString("EXPLORE INDIA'S LARGEST ONLINE STORE"))
            ),
            Seq(
              PageWork(ScrapeCrawlLinks(ScrapeLinksBy.BY_TEXT.toString, "Toys & Games"))
            )
          ),
          WebpageIdentifier(
            PageUniqueness(
              "https://www\\.amazon\\.in/.+",
              Seq(
                UniqueTag(
                  "#p_72-title",
                  Some("Avg. Customer Review"),
                  Some(Element(FindElementBy.BY_ID.toString, "#reviewsRefinements"))
                ),
                UniqueTag(
                  "ul.a-unordered-list.a-nostyle.a-vertical.a-spacing-medium",
                  Some("Toys & Games"),
                  Some(Element(FindElementBy.BY_ID.toString, "#departments"))
                )
              ),
              Seq(UniqueString("Sort by:", Some(Element(FindElementBy.BY_ID.toString, "#a-autoid-0-announce"))))
            ),
            Seq(
              PageWork(ScrapeCrawlLinks(ScrapeLinksBy.BY_SELECTOR.toString, "a.a-link-normal.a-text-normal")),
              PageWork(ScrapeCrawlLinks(ScrapeLinksBy.BY_SELECTOR.toString, "li.a-normal a"), Some(Element(FindElementBy.BY_CSS_SELECTOR.toString, "div.a-text-center[role=\"navigation\"]")))
            )
          ),
          WebpageIdentifier(
            PageUniqueness(
              "https://www\\.amazon\\.in/.+",
              Seq(
                UniqueTag(
                  "span.nav-a-content",
                  Some("Toys & Games"),
                  Some(Element(FindElementBy.BY_ID.toString, "#nav-progressive-subnav"))
                ),
                UniqueTag(
                  "#wishListMainButton-announce",
                  Some("Add to Wish List"),
                  Some(Element(FindElementBy.BY_ID.toString, "#addToWishlist_feature_div"))
                )
              ),
              Seq(UniqueString("Toys & Games", Some(Element(FindElementBy.BY_ID.toString, "#nav-progressive-subnav"))))
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
    logger.info("Started listening on http://0.0.0.0:8082/")

  }

}
