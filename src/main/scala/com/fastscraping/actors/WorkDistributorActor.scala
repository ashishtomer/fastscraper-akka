package com.fastscraping.actors

import akka.actor.typed.{ActorRef, ActorSystem, Behavior}
import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{ContentTypes, HttpEntity}
import com.fastscraping.actors.PrinterActor.{PrintMessage, PrintStringOnConsole}
import akka.http.scaladsl.server.Directives._

object WorkDistributorActor {

  def apply(): Behavior[Nothing] = Behaviors.setup[Nothing] { context =>
    val actorsChild: ActorRef[PrintMessage] = context.spawn(PrinterActor(), "print-actor")

    listenHttpCalls(actorsChild)(context.system)

    Behaviors.empty[Nothing]
  }

  private def listenHttpCalls(actorRef: ActorRef[PrintMessage])(implicit as: ActorSystem[Nothing]): Unit = {
    val route = path("scrape") {
      get {
        println("Get received on the hello")
        actorRef ! PrintStringOnConsole
        complete(HttpEntity(ContentTypes.`application/json`, "{\"message\":\"Scraping started\"}"))
      }
    }

    val bindingFuture = Http().newServerAt("localhost", 8080).bind(route)

  }

}
