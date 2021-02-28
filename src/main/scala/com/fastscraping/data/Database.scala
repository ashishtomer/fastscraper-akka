package com.fastscraping.data

import com.fastscraping.data.bson.CrawlLink
import org.bson.Document

import scala.collection.mutable
import scala.concurrent.{ExecutionContext, Future}

trait Database {
  /**
   * @param collection The collection or table in the database
   * @param index Name of the column or index under which the text will be stored
   * @param text The text data itself
   * @param documentId Find the document with `documentId` and save text under
   */
  def saveText(collection: String, documentId: String, index: String, text: String)

  def saveDocument(collection: String, documentId: String, doc: Map[String, AnyRef])

  def saveDocument(collection: String, documentId: String, doc: Document)

  def saveDocument(collection: String, doc: Document)

  def saveDocument(collection: String, doc: Map[String, AnyRef])

  def saveDocuments(collection: String, docs: Seq[Map[String, AnyRef]])

  def isLinkScraped(jobId: Option[String], link: String): Boolean

  def nextScrapeLinks(jobId: Option[String], limit: Int = 1)(implicit ec: ExecutionContext): mutable.Buffer[CrawlLink]

  def markLinkAsScraped(jobId: Option[String], link: String)
}

object Database {
  case class NextScrapeLink(link: String)
}
