package com.fastscraping.actor.message

import java.net.URL
import java.util.UUID

import com.fastscraping.model.WebpageIdentifier
import play.api.libs.json.Json

trait LinkManagerActorMessage

class ScrapeJob(
                 val seedURL: String,
                 val webpageIdentifiers: Seq[WebpageIdentifier],
                 val jobId: String = UUID.randomUUID().toString
               ) extends LinkManagerActorMessage

object ScrapeJob {
  implicit val fmt = Json.format[ScrapeJob]

  def apply(seedURL: String, webpageIdentifiers: Seq[WebpageIdentifier], jobId: String): ScrapeJob = {
    new ScrapeJob(seedURL, webpageIdentifiers, jobId)
  }

  def apply(seedURL: String, webpageIdentifiers: Seq[WebpageIdentifier]): ScrapeJob = {
    new ScrapeJob(createCorrectUrl(seedURL), webpageIdentifiers, UUID.randomUUID().toString)
  }

  def unapply(arg: ScrapeJob): Option[(String, Seq[WebpageIdentifier], String)] = {
    Some((arg.seedURL, arg.webpageIdentifiers, arg.jobId))
  }

  private def createCorrectUrl(url: String) = {
    val urlWithProtocol = if (!url.startsWith("http://") || !url.startsWith("https://")) {
      "http://" + url
    } else {
      url
    }

    new URL(urlWithProtocol).toString
  }

}
