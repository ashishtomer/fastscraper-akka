package com.fastscraping.data

import com.fastscraping.data.Database.NextScrapeLink
import org.mongodb.scala.Document

import scala.concurrent.{ExecutionContext, Future}

trait Database {
  /**
   * @param index The index (or table or collection) in the database
   * @param column Name of the column under which the text will be stored
   * @param text The text data itself
   * @param documentId Find the document with `documentId` and save text under
   */
  def saveText(index: String, documentId: String, column: String, text: String)

  def saveDocument(index: String, documentId: String, doc: Document)

  def nextScrapeLinks(limit: Int = 1)(implicit ec: ExecutionContext): Future[Seq[String]]

  def markLinkAsScraped(link: String)
}

object Database {
  case class NextScrapeLink(link: String)
}
