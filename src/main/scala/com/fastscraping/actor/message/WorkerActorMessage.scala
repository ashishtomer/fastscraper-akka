package com.fastscraping.actor.message

import com.fastscraping.data.Database
import com.fastscraping.model.WebpageIdentifier

import scala.collection.mutable.ListBuffer

sealed trait WorkerActorMessage

case class StartScraping(link: String, jobId: String, webpageIdentifier: Seq[WebpageIdentifier]) extends WorkerActorMessage

case class ScrapeNextPage(webpageIdentifiers: ListBuffer[WebpageIdentifier], jobId: Option[String], db: Database) extends WorkerActorMessage
