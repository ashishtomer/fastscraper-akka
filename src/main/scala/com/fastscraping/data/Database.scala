package com.fastscraping.data

import com.fastscraping.data.bson.CrawlLink
import play.api.libs.json.JsValue

import scala.collection.mutable
import scala.concurrent.{ExecutionContext, Future}

trait Database {
  /**
   * @param index The index (or table or collection) in the database
   * @param column Name of the column under which the text will be stored
   * @param text The text data itself
   * @param documentId Find the document with `documentId` and save text under
   */
  def saveText(index: String, documentId: String, column: String, text: String)

  def saveDocument(index: String, documentId: String, doc: Map[String, AnyRef])

  def isLinkScraped(jobId: Option[String], link: String): Boolean

  def nextScrapeLinks(jobId: Option[String], limit: Int = 1)(implicit ec: ExecutionContext): mutable.Buffer[CrawlLink]

  def markLinkAsScraped(jobId: Option[String], link: String)
}

object Database {
  case class NextScrapeLink(link: String)
}
