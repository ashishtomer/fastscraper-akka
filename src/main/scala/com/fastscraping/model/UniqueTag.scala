package com.fastscraping.model

import play.api.libs.json.Json

case class UniqueTag(selector: String, text: Option[String])


object UniqueTag {
  implicit val fmt = Json.format[UniqueTag]
}
