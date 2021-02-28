package com.fastscraping.actor

import akka.actor
import akka.actor.Status.Success
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{ContentTypes, HttpEntity}
import akka.http.scaladsl.server.Directives._
import akka.stream.Materializer
import com.fastscraping.actor.message.{LinkManagerActorMessage, ScrapeJob}
import com.fastscraping.model.ActionName._
import com.fastscraping.model._
import com.fastscraping.pagenavigation.action.FindElementAction
import com.fastscraping.pagenavigation.scrape
import com.fastscraping.pagenavigation.scrape.CleanseLinkMethod._
import com.fastscraping.pagenavigation.scrape.ScrapeCrawlLinks.ScrapeLinksBy._
import com.fastscraping.pagenavigation.scrape.{CleanseLink, ScrapeCrawlLinks, ScrapeData}
import com.fastscraping.pagenavigation.selenium.ElementFinder.FindElementBy
import com.fastscraping.server.SetupServer
import com.fastscraping.utils.FsLogging

object HttpListeningActor extends FsLogging {

  def apply(): Behavior[Nothing] = Behaviors.setup[Nothing] { context =>
    val linkManagerActor: ActorRef[LinkManagerActorMessage] = context.spawn(LinkManagerActor(), "link-manager")

    listenHttpCalls(linkManagerActor)(context.system.classicSystem)

    Behaviors.empty[Nothing]
  }

  private def listenHttpCalls(linkManager: ActorRef[LinkManagerActorMessage])(implicit as: actor.ActorSystem): Unit = {
    implicit val mat: Materializer = Materializer(as)
    import as.dispatcher
    SetupServer(linkManager).onComplete(_ => logger.info("Started listening on http://0.0.0.0:8082/"))
      /*get {
        path("scrape") {
          val idfs = Seq(
            WebpageIdentifier(
              PageUniqueness("https://www\\.amazon\\.in/b\\?ie=UTF8&node=6308595031",
                Seq(UniqueTag("div.acs_widget-title.acs_widget-title__secondary", None, None)),
                Seq(UniqueString("EXPLORE INDIA'S LARGEST ONLINE STORE"))
              ),
              Seq(
                PageWork(scrape.NextPage(FindElementAction(CLICK, FindElementBy.LINK_TEXT, "Toys & Games")), Some(Element(FindElementBy.ID, "categoryTilesSoftlines_127966")))
              )
            ),
            WebpageIdentifier(
              PageUniqueness(
                "https://www\\.amazon\\.in/.+",
                Seq(
                  UniqueTag(
                    "#p_72-title",
                    Some("Avg. Customer Review"),
                    Some(Element(FindElementBy.ID, "reviewsRefinements"))
                  ),
                  UniqueTag(
                    "ul.a-unordered-list.a-nostyle.a-vertical.a-spacing-medium",
                    Some("Toys & Games"),
                    Some(Element(FindElementBy.ID, "departments"))
                  )
                ),
                Seq(UniqueString("Sort by:", Some(Element(FindElementBy.ID, "a-autoid-0-announce"))))
              ),
              Seq(
                PageWork(ScrapeCrawlLinks(BY_SELECTOR, "a.a-link-normal.a-text-normal", Some(CleanseLink(SUBSTRING_TILL, "/ref=")))),
                PageWork(ScrapeCrawlLinks(BY_SELECTOR, "li.a-last a", Some(CleanseLink(SUBSTRING_TILL, "/ref="))))
              )
            ),
            WebpageIdentifier(
              PageUniqueness(
                "https://www\\.amazon\\.in/.+",
                Seq(
                  UniqueTag(
                    "span.nav-a-content",
                    Some("Toys & Games"),
                    Some(Element(FindElementBy.ID, "nav-progressive-subnav"))
                  ),
                  UniqueTag(
                    "#wishListMainButton-announce",
                    Some("Add to Wish List"),
                    Some(Element(FindElementBy.ID, "addToWishlist_feature_div"))
                  )
                ),
                Seq(UniqueString("Toys & Games", Some(Element(FindElementBy.ID, "nav-progressive-subnav"))))
              ),
              Seq(
                PageWork(ScrapeData(FindElementBy.ID, "productTitle", DataToExtract("product_name", ScrapeDataTypes.TEXT), "toys")),
                PageWork(ScrapeData(FindElementBy.ID, "wayfinding-breadcrumbs_container", DataToExtract("hierarchy", ScrapeDataTypes.TEXT), "toys")),
                PageWork(ScrapeData(FindElementBy.ID, "acrCustomerReviewText", DataToExtract("number_of_ratings", ScrapeDataTypes.TEXT), "toys")),
                PageWork(ScrapeData(FindElementBy.ID, "askATFLink", DataToExtract("number_of_questions", ScrapeDataTypes.TEXT), "toys")),
                PageWork(ScrapeData(FindElementBy.ID, "priceblock_ourprice_row", DataToExtract("price", ScrapeDataTypes.TEXT), "toys")),
                PageWork(ScrapeData(FindElementBy.ID, "regularprice_savings", DataToExtract("saving", ScrapeDataTypes.TEXT), "toys")),
                PageWork(ScrapeData(FindElementBy.ID, "sellerProfileTriggerId", DataToExtract("sold_by", ScrapeDataTypes.TEXT), "toys")),
                PageWork(ScrapeData(FindElementBy.ID, "SSOFpopoverLink", DataToExtract("fulfilled_by", ScrapeDataTypes.TEXT), "toys")),
                PageWork(ScrapeData(FindElementBy.ID, "productDetails_techSpec_section_1", DataToExtract("product_information", ScrapeDataTypes.TEXT), "toys")),
                PageWork(ScrapeData(FindElementBy.ID, "productDetails_detailBullets_sections1", DataToExtract("additional_information", ScrapeDataTypes.TEXT), "toys")),
                PageWork(ScrapeData(FindElementBy.CSS_SELECTOR, "span.a-declarative", DataToExtract("sold_by_others", ScrapeDataTypes.TEXT), "toys"), Some(Element(FindElementBy.ID, "olp_feature_div"))),
                PageWork(ScrapeData(FindElementBy.CSS_SELECTOR, "ul.a-unordered-list.a-vertical.a-spacing-mini", DataToExtract("product_description", ScrapeDataTypes.TEXT), "toys"), Some(Element(FindElementBy.ID, "feature-bullets"))),
                PageWork(ScrapeData(FindElementBy.CSS_SELECTOR, "fieldset.forScreenreaders", DataToExtract("frequently_bought_together", ScrapeDataTypes.TEXT), "toys"), Some(Element(FindElementBy.ID, "sims-fbt-form")))
              )
            )
          )

          val scrapeJob = ScrapeJob("https://www.amazon.in/b?ie=UTF8&node=6308595031", idfs, Some("https://www.amazon.in/gp/slredirect/picassoRedirect.html"))
          println(scrapeJob.toJson)

          complete(HttpEntity(ContentTypes.`application/json`, "{\"message\":\"Scraping started\"}"))
        }
      }*/
  }

}
