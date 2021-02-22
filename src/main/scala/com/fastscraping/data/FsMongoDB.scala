package com.fastscraping.data

import java.util

import com.fastscraping.data.bson.CrawlLink
import com.fastscraping.utils.Miscellaneous._
import com.mongodb.client.model.{Filters, ReplaceOptions}
import com.mongodb.client.{MongoCollection, MongoDatabase}
import org.bson.Document

import scala.concurrent.ExecutionContext
import scala.jdk.CollectionConverters._
import scala.util.{Failure, Success, Try}

class FsMongoDB(db: MongoDatabase) extends Database {
  private def WithCollection[T](collectionName: String)(f: MongoCollection[Document] => T): T = {
    f(db.getCollection(collectionName))
  }

  private val doReplaceUpsert = new ReplaceOptions().upsert(true)
  private val id = "doc_id"

  override def saveText(collection: String, documentId: String, index: String, text: String): Unit = {
    WithCollection(collection) { mongoCollection =>
      IgnoreDuplication {
        mongoCollection.replaceOne(
          Filters.eq(id, documentId),
          new Document(
            Map(
              id -> documentId,
              index -> text.asInstanceOf[AnyRef]
            ).asJava),
          doReplaceUpsert
        )
      }
    }
  }

  override def saveDocument(collection: String, documentId: String, doc: Map[String, AnyRef]) = Try {
    WithCollection(collection) { mongoCollection =>
      IgnoreDuplication {
        mongoCollection.replaceOne(
          Filters.eq(id, documentId),
          new Document(doc.asJava),
          doReplaceUpsert
        )
      }
    }
  }

  override def saveDocument(collection: String, documentId: String, doc: Document) = Try {
    WithCollection(collection)(mongoCollection =>
      IgnoreDuplication(mongoCollection.replaceOne(Filters.eq(id, documentId), doc, doReplaceUpsert))
    )
  }

  override def nextScrapeLinks(jobId: Option[String], limit: Int = 1)(implicit ec: ExecutionContext) = {
    val javaArray = new util.ArrayList[CrawlLink](limit)
    WithCollection(CrawlLinkCollection(jobId)) { mongoCollection =>
      mongoCollection.find(Filters.eq(IS_CRAWLED, false), classOf[CrawlLink])
        .limit(limit)
        .into(javaArray)
      javaArray.asScala
    }
  }

  override def markLinkAsScraped(jobId: Option[String], link: String): Unit = {
    WithCollection(CrawlLinkCollection(jobId)) { mongoCollection =>
      IgnoreDuplication {
        mongoCollection.replaceOne(
          Filters.eq(_LINK_TO_CRAWL, link),
          new Document(CrawlLink(link, true).asJavaMap),
          doReplaceUpsert
        )
      }
    }
  }

  def IgnoreDuplication[T](f: => T) = Try(f) match {
    case Success(x) => x
    case Failure(exception) if exception.getMessage.contains("duplicate key error") =>
    case Failure(exception) => throw exception
  }

  override def isLinkScraped(jobId: Option[String], link: String): Boolean = {
    WithCollection(CrawlLinkCollection(jobId)) { mongoCollection =>
      val element = mongoCollection
        .find(Filters.and(Filters.eq(_LINK_TO_CRAWL, link), Filters.eq(IS_CRAWLED, true)))
        .first()

      element != null
    }
  }
}

object FsMongoDB {
  def apply(db: MongoDatabase): FsMongoDB = new FsMongoDB(db)
}
