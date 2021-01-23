package com.fastscraping.model

import play.api.libs.json.Json

case class WebpageIdentifier(urlRegex: String,
                             uniqueStringOnPage: Option[String] = None,
                             uniqueTag: UniqueTag,
                             actionsAndScrapeData: Seq[ActionsAndScrapeData])

object WebpageIdentifier {
  implicit val fmt = Json.format[WebpageIdentifier]
}
