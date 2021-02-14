package com.fastscraping.actor.message

import com.fastscraping.data.Database
import com.fastscraping.model.WebpageIdentifier

sealed trait WorkerActorMessage

case class StartScraping(link: String, jobId: String, webpageIdentifier: Seq[WebpageIdentifier]) extends WorkerActorMessage
case class ScrapeNextPage(webpageIdentifiers: Seq[WebpageIdentifier], db: Database) extends WorkerActorMessage
