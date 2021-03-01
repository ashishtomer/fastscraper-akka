package com.fastscraping.server

import akka.actor.ActorSystem
import akka.actor.typed.ActorRef
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.{ContentTypes, HttpEntity}
import akka.http.scaladsl.server.{Directives, Route}
import akka.stream.Materializer
import com.fastscraping.actor.message.{LinkManagerActorMessage, ScrapeJob}

import scala.concurrent.Future

object SetupServer extends Directives with SprayJsonSupport {

  def route(linkManagerActor: ActorRef[LinkManagerActorMessage]): Route = {
    post {
      entity(as[ScrapeJob]) {
        scrapeJob: ScrapeJob =>
          linkManagerActor ! scrapeJob
          complete(HttpEntity(ContentTypes.`application/json`, "{\"message\":\"Scraping started with job ID:\"}"))
      }
    }
  }


  def apply(linkManagerActor: ActorRef[LinkManagerActorMessage])(implicit as: ActorSystem, mat: Materializer): Future[Http.ServerBinding] = {
    Http()(as).newServerAt("127.0.0.1", 8082).bind(route(linkManagerActor))
  }
}
