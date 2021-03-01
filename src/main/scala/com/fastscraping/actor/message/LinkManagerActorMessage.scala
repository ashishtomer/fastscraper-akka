package com.fastscraping.actor.message

import java.net.URL
import java.util.UUID

import com.fastscraping.model.WebpageIdentifier
import spray.json.DefaultJsonProtocol._
import spray.json.{JsValue, RootJsonFormat}

trait LinkManagerActorMessage {
  def toJson: JsValue
}

case class ScrapeJob(seedURL: String,
                     webpageIdentifiers: Seq[WebpageIdentifier],
                     blockingUrl: Option[String] = None,
                     jobId: String = UUID.randomUUID().toString
                    ) extends LinkManagerActorMessage {
  override def toJson: JsValue = ScrapeJob.sprayJsonFmt.write(this)
}

object ScrapeJob {
  implicit val sprayJsonFmt: RootJsonFormat[ScrapeJob] = jsonFormat4(ScrapeJob.apply)

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
