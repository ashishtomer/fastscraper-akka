package com.fastscraping.utils

import akka.actor.typed.ActorSystem
import com.fastscraping.actor.HttpListeningActor

object StartApplication extends FsLogging {

  def apply() = {
    logger.info("Setting up drivers for browsers")

    System.setProperty("webdriver.chrome.driver", "/opt/fastscraper/chromedriver")
    System.setProperty("webdriver.gecko.driver", "/opt/fastscraper/geckodriver")

    ActorSystem[Nothing](HttpListeningActor(), "fastscraping")
  }

}
