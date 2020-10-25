package com.fastscraping.utils

import akka.actor.typed.ActorSystem
import com.fastscraping.actors.WorkDistributorActor

object StartApplication {

  def apply() = {
    val actorSystem = ActorSystem[Nothing](WorkDistributorActor(), "fastscraping")
  }

}
