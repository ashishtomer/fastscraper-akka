package com.fastscraping.actor.message

import java.net.URL
import java.util.UUID

import com.fastscraping.model.WebpageIdentifier
import play.api.libs.json.Json

trait LinkManagerActorMessage

case class ScrapeJob(seedURL: String,
                webpageIdentifiers: Seq[WebpageIdentifier],
                blockingUrl: Option[String] = None,
                jobId: String = UUID.randomUUID().toString
               ) extends LinkManagerActorMessage

object ScrapeJob {
  implicit val fmt = Json.format[ScrapeJob]

  def apply(seedURL: String, webpageIdentifiers: Seq[WebpageIdentifier], jobId: String): ScrapeJob = {
    new ScrapeJob(seedURL, webpageIdentifiers, jobId = jobId)
  }

  def apply(seedURL: String, webpageIdentifiers: Seq[WebpageIdentifier], blockingUrl: Option[String]): ScrapeJob = {
    new ScrapeJob(seedURL, webpageIdentifiers, blockingUrl, UUID.randomUUID().toString)
  }

  def apply(seedURL: String, webpageIdentifiers: Seq[WebpageIdentifier]): ScrapeJob = {
    new ScrapeJob(createCorrectUrl(seedURL), webpageIdentifiers, jobId = UUID.randomUUID().toString)
  }

  private def createCorrectUrl(url: String) = {
    val urlWithProtocol = if (url.startsWith("http://") || url.startsWith("https://")) {
      url
    } else {
      "http://" + url
    }

    new URL(urlWithProtocol).toString
  }

}
