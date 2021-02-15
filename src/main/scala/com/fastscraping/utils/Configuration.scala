package com.fastscraping.utils

import java.util.Properties

import com.typesafe.config.ConfigFactory

case class Configuration(properties: Properties)

object Configuration {
  lazy val config = ConfigFactory.load()
}
