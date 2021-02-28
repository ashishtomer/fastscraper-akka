package com.fastscraping.model

import spray.json.DefaultJsonProtocol._
import spray.json.JsonFormat

case class WebpageIdentifier(pageUniqueness: PageUniqueness, pageWorks: Seq[PageWork])

object WebpageIdentifier {
  implicit val sprayJsonFmt: JsonFormat[WebpageIdentifier] = jsonFormat2(WebpageIdentifier.apply)

}
