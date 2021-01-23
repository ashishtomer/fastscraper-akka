package com.fastscraping.utils

import akka.actor.typed.ActorSystem
import com.fastscraping.actor.HttpListeningActor

object StartApplication {

  def apply() = {
    System.setProperty("webdriver.chrome.driver", "./chromedriver")
    System.setProperty("webdriver.gecko.driver", "./geckodriver")
    val actorSystem = ActorSystem[Nothing](HttpListeningActor(), "fastscraping")
  }

}
