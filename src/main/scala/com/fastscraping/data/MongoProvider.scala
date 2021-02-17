package com.fastscraping.data

import java.util.concurrent.TimeUnit

import com.fastscraping.utils.Miscellaneous.{CRAWL_LINK_INDEX, CrawlLinkCollection}
import com.mongodb.client.model.IndexOptions
import com.mongodb.client.{MongoClient, MongoClients, MongoDatabase}
import com.mongodb.connection.{ClusterSettings, ConnectionPoolSettings}
import com.mongodb.{MongoClientSettings, MongoCredential, ServerAddress}
import org.bson.Document

import scala.jdk.CollectionConverters._

object MongoProvider {
  private var client: MongoClient = _
  private var fsScrapedDataDb: MongoDatabase = _

  private def getClientAndDb(jobId: Option[String]): MongoDatabase = synchronized {

    if (client != null) {
      val existingCollections = new java.util.ArrayList[String]()
      fsScrapedDataDb.listCollectionNames().into(existingCollections)
      if (existingCollections.contains(s"${CrawlLinkCollection(jobId)}")) return fsScrapedDataDb
    }

    import com.fastscraping.utils.MongoConfig._

    val mongoSeedNodes = hosts.map { host =>
      val ipAndPort = host.split(":")
      new ServerAddress(ipAndPort(0), ipAndPort(1).toInt)
    }

    val clusterSettings = ClusterSettings.builder().hosts(mongoSeedNodes.asJava).build()

    val poolSettings = ConnectionPoolSettings.builder()
      .minSize(maxConnPoolSize)
      .maxSize(maxConnPoolSize)
      .maxConnectionIdleTime(maxConnIdleSeconds, TimeUnit.SECONDS)
      .build()

    val credential = MongoCredential.createScramSha1Credential(userName, scrapedDataDb, password.toCharArray)

    val settings = MongoClientSettings.builder()
      .applyToClusterSettings(builder => builder.applySettings(clusterSettings))
      .applyToConnectionPoolSettings(builder => builder.applySettings(poolSettings))
      //      .streamFactoryFactory(NettyStreamFactoryFactory.builder().build())
      //      .credential(credential)
      .codecRegistry(MongoCodecRegistries.getCodecRegistries)
      .build()

    client = MongoClients.create(settings)

    fsScrapedDataDb = client.getDatabase(scrapedDataDb)

    fsScrapedDataDb
      .getCollection(s"${CrawlLinkCollection(jobId)}")
      .createIndex(
        new Document(Map(CRAWL_LINK_INDEX -> 1).asInstanceOf[Map[String, AnyRef]].asJava),
        new IndexOptions().unique(true)
      )

    fsScrapedDataDb
  }

  def getDatabase(jobId: Option[String]): MongoDatabase = getClientAndDb(jobId)

}
