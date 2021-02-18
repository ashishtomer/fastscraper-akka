package com.fastscraping.model

import play.api.libs.json.Json

case class PageUniqueness(urlRegex: String, uniqueTags: Seq[UniqueTag], uniqueStrings: Seq[UniqueString])

object PageUniqueness {
  implicit val fmt = Json.format[PageUniqueness]
}
