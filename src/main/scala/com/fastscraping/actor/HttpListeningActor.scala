package com.fastscraping.actor

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, ActorSystem, Behavior}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{ContentTypes, HttpEntity}
import akka.http.scaladsl.server.Directives._
import com.fastscraping.actor.message.{LinkManagerActorMessage, ScrapeJob}
import com.fastscraping.model.ScrapeDataTypes._
import com.fastscraping.model.{DataToExtract, PageWork, UniqueTag, WebpageIdentifier}
import com.fastscraping.pagenavigation.action.{FindElementActions, KeySelectorActions, SelectorActions}
import com.fastscraping.pagenavigation.scrape.ScrapeData

object HttpListeningActor {

  def apply(): Behavior[Nothing] = Behaviors.setup[Nothing] { context =>
    val linkManagerActor: ActorRef[LinkManagerActorMessage] = context.spawn(LinkManagerActor(), "link-manager")

    listenHttpCalls(linkManagerActor)(context.system)

    Behaviors.empty[Nothing]
  }

  private def listenHttpCalls(linkManager: ActorRef[LinkManagerActorMessage])(implicit as: ActorSystem[Nothing]): Unit = {
    val route = path("scrape") {
      get {
        println("Get received on the hello")
        val idf = WebpageIdentifier(
          urlRegex = ".*https://www.binance.com/.*",
          uniqueStringOnPage = Some("Buy & sell Crypto in minutes"),
          UniqueTag("div.css-1gmkfzs", None),
          Seq(
            PageWork(SelectorActions("CLICK", Some("a#ba-tableMarkets"))),
            PageWork(FindElementActions("CLICK", "BY_TEXT", "FIAT Markets"))
          )
        )

        linkManager ! ScrapeJob("https://www.binance.com", Seq(idf), "binance")
        complete(HttpEntity(ContentTypes.`application/json`, "{\"message\":\"Scraping started\"}"))
      }
    }

    val bindingFuture = Http().newServerAt("0.0.0.0", 8082).bind(route)

  }

}
