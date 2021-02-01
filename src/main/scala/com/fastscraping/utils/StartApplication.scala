package com.fastscraping.utils

import akka.actor.typed.ActorSystem
import com.fastscraping.actor.HttpListeningActor

object StartApplication {

  def apply() = {
    System.setProperty("webdriver.chrome.driver", "/opt/fastscraper/chromedriver")
    System.setProperty("webdriver.gecko.driver", "/opt/fastscraper/geckodriver")
    val actorSystem = ActorSystem[Nothing](HttpListeningActor(), "fastscraping")
  }

}
