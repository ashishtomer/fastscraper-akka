package com.fastscraping.model

import com.fastscraping.pagenavigation.ActionsAndScrapeData
import play.api.libs.json.Json

case class WebpageIdentifier(urlRegex: String,
                             uniqueStringOnPage: Option[String] = None,
                             uniqueTag: UniqueTag,
                             actionsAndScrapeData: Seq[ActionsAndScrapeData])

object WebpageIdentifier {
  implicit val fmt = Json.format[WebpageIdentifier]
}
