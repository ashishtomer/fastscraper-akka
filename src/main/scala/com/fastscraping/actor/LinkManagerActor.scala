package com.fastscraping.actor


import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import akka.actor.typed.{ActorRef, Behavior}
import com.fastscraping.actor.message.{LinkManagerActorMessage, ScrapeJob, ScrapeWebpage, WorkerActorMessage}

import scala.collection.mutable

class LinkManagerActor(context: ActorContext[LinkManagerActorMessage]) extends AbstractBehavior[LinkManagerActorMessage](context) {
  private val workerActorsCache = mutable.Map[String, ActorRef[WorkerActorMessage]]()

  private def getWorkerActor(actorName: String) = {
    workerActorsCache.get(actorName) match {
      case Some(actorRef) => actorRef
      case None =>
        val actorRef: ActorRef[WorkerActorMessage] = context.spawn(WorkerActor(), actorName)
        workerActorsCache(actorName) = actorRef
        actorRef

    }
  }

  override def onMessage(msg: LinkManagerActorMessage): Behavior[LinkManagerActorMessage] = {
    msg match {
      case job @ ScrapeJob(seedURL, webpageIdentifiers, jobId) =>

        getWorkerActor(s"worker_actor_$jobId") ! ScrapeWebpage(seedURL, jobId, webpageIdentifiers)

        Behaviors.same[LinkManagerActorMessage]
    }
  }
}

object LinkManagerActor {
  def apply() = Behaviors.setup[LinkManagerActorMessage](context => new LinkManagerActor(context))
}