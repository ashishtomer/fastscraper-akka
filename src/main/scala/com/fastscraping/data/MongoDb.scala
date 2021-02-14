package com.fastscraping.data

import com.fastscraping.utils.Miscellaneous._
import com.mongodb.client.model.{Filters, IndexOptions, ReplaceOptions}
import com.mongodb.client.{MongoClient, MongoClients, MongoCollection}
import com.mongodb.{MongoClientSettings, ServerAddress}
import org.bson.Document

import scala.concurrent.{ExecutionContext, Future}
import scala.jdk.CollectionConverters._

//Single instance for whole application
//Each website which is being scraped can have its own database
//Or the client can use the databases as needed
class MongoDb(address: String, port: Int, clientDb: String) extends Database {
  private lazy val db = MongoDb.getSingleton(address, port).getDatabase(clientDb)

  private def WithCollection[T](collectionName: String)(f: MongoCollection[Document] => T): T = {
    f(db.getCollection(collectionName))
  }

  private def ObserveDbTransaction[T](id: Option[String])(f: => Observable[T]): Unit = {
    f.subscribe(new Observer[T] {
      override def onNext(result: T): Unit = println(s"[db=$db][id=$id]MongoDb transaction completed with: $result")

      override def onError(e: Throwable): Unit = println(s"[db=$db][id=$id]Error while completing transaction ${e.getMessage}")

      override def onComplete(): Unit = println(s"[db=$db][id=$id]Mongodb transaction completed")
    })
  }

  private val doReplaceUpsert = new ReplaceOptions().upsert(true)
  private val id = "doc_id"

  override def saveText(collection: String, documentId: String, column: String, text: String): Unit = {
    WithCollection(collection) { mongoCollection =>
      ObserveDbTransaction(Some(documentId)) {
        mongoCollection.replaceOne(
          Filters.eq(id, documentId),
          Document(id -> documentId, column -> text),
          doReplaceUpsert
        )
      }
    }
  }

  override def saveDocument(collection: String, documentId: String, doc: Document): Unit = {
    WithCollection(collection) { mongoCollection =>
      ObserveDbTransaction(Some(documentId)) {
        mongoCollection.replaceOne(
          Filters.eq(id, documentId),
          doc,
          doReplaceUpsert
        )
      }
    }
  }

  override def nextScrapeLinks(limit: Int = 1)(implicit ec: ExecutionContext) = {
    WithCollection(CRAWL_LINK_COLLECTION) { mongoCollection =>
      mongoCollection.find(Filters.eq(IS_CRAWLED, false))
        .subscribe(document => println(document))
    }

    Future(Seq.empty)
  }

  override def markLinkAsScraped(link: String): Unit = {
    WithCollection(CRAWL_LINK_COLLECTION) { mongoCollection =>
      ObserveDbTransaction(Some(CRAWL_LINK_INDEX)) {
        mongoCollection.replaceOne(
          Filters.eq(CRAWL_LINK_INDEX, link),
          Document(CRAWL_LINK_INDEX -> link, IS_CRAWLED -> true),
          doReplaceUpsert
        )
      }
    }
  }
}

object MongoDb {
  private var mongoClient: MongoClient = _

  case class MongoDbResult[T](res: Seq[T])

  private def getSingleton(address: String, port: Int) = if (mongoClient != null) {
    mongoClient
  } else {
    synchronized {
      if (mongoClient == null) {
        mongoClient = MongoClients.create(
          MongoClientSettings.builder()
            .applyToClusterSettings(builder => builder.hosts(List(new ServerAddress(address, port)).asJava))
            .build()
        )
      }

      mongoClient
    }
  }

  def apply(address: String, port: Int, clientDb: String = "fastscraper"): MongoDb = {
    MongoDb.getSingleton(address, port)
      .getDatabase(clientDb)
      .getCollection(CRAWL_LINK_COLLECTION)
      .createIndex(Document(CRAWL_LINK_INDEX -> 1), new IndexOptions().unique(true))

    new MongoDb(address, port, clientDb)
  }

}
