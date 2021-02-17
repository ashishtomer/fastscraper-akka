package com.fastscraping.model

import play.api.libs.json.Json

case class UniqueString(string: String, contextElement: Option[Element] = None)

object UniqueString {
  implicit val fmt = Json.format[UniqueString]
}
