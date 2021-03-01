package com.fastscraping.actor

import akka.actor
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior}
import akka.stream.Materializer
import com.fastscraping.actor.message.LinkManagerActorMessage
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
  }

}
