package com.fastscraping.utils

import scala.jdk.CollectionConverters._

object MongoConfig {

  import Configuration._

  lazy val hosts: Seq[String] = config.getStringList("mongo.connection.hosts").asScala.toSeq
  lazy val minConnPoolSize: Int = config.getInt("mongo.connection.min-pool-size")
  lazy val maxConnPoolSize: Int = config.getInt("mongo.connection.max-pool-size")
  lazy val maxConnIdleSeconds: Int = config.getInt("mongo.connection.max-idle-seconds")
  lazy val maxWaitQueueSize: Int = config.getInt("mongo.connection.max-wait-queue-size")
  lazy val userName: String = config.getString("mongo.connection.username")
  lazy val password: String = config.getString("mongo.connection.password")
  lazy val scrapedDataDb: String = config.getString("mongo.scraped-data-db")

}
