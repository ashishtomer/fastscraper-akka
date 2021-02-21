package com.fastscraping.utils

import org.slf4j.LoggerFactory

trait FsLogging {
  protected lazy val logger = LoggerFactory.getLogger(getClass)
}
