package com.fastscraping.actor

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, ActorSystem, Behavior}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{ContentTypes, HttpEntity}
import akka.http.scaladsl.server.Directives._
import com.fastscraping.actor.message.{LinkManagerActorMessage, ScrapeJob}
import com.fastscraping.model.actions.{KeySelectorActions, SelectorActions}
import com.fastscraping.model.{UniqueTag, WebpageIdentifier}

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
          urlRegex = ".*youtube\\.com.*",
          uniqueStringOnPage = Some("हिन्दी"),
          UniqueTag("img#hplogo", None),
          Seq(
            SelectorActions("CLICK", Some("input")),
            KeySelectorActions("SEND_KEYS", "Badshah Music", None),
            KeySelectorActions("SEND_KEYS", "ARROW_DOWN", None, Some(5)),
            KeySelectorActions("SEND_KEYS", "ENTER", None),
          )
        )

        linkManager ! ScrapeJob("youtube.com", Seq(idf))
        complete(HttpEntity(ContentTypes.`application/json`, "{\"message\":\"Scraping started\"}"))
      }
    }

    val bindingFuture = Http().newServerAt("0.0.0.0", 8082).bind(route)

  }

}
