package com.fastscraping.model

import play.api.libs.json.Json

case class WebpageIdentifier(pageUniqueness: PageUniqueness, pageWorks: Seq[PageWork])

object WebpageIdentifier {
  implicit val fmt = Json.format[WebpageIdentifier]
}
