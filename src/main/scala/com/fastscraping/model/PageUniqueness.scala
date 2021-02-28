package com.fastscraping.model

import spray.json.DefaultJsonProtocol._
import spray.json.JsonFormat

case class PageUniqueness(urlRegex: String, uniqueTags: Seq[UniqueTag], uniqueStrings: Seq[UniqueString])

object PageUniqueness {
  implicit val sprayJsonFmt: JsonFormat[PageUniqueness] = jsonFormat3(PageUniqueness.apply)
}
