package com.fastscraping.model

import play.api.libs.json.Json

case class PageUniqueness(urlRegex: String, uniqueTag: UniqueTag, uniqueString: UniqueString)

object PageUniqueness {
  implicit val fmt = Json.format[PageUniqueness]
}
