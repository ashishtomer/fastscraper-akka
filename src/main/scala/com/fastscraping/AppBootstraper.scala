package com.fastscraping

import com.fastscraping.utils.{FsLogging, StartApplication}


object AppBootstraper extends FsLogging {

  def main(args: Array[String]): Unit = {
    logger.info("Starting the application")
    StartApplication()
  }
}
