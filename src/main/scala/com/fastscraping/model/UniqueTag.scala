package com.fastscraping.model

import play.api.libs.json.Json

case class UniqueTag(selector: String, text: Option[String], contextElement: Option[Element] = None)

object UniqueTag {
  implicit val fmt = Json.format[UniqueTag]
}
