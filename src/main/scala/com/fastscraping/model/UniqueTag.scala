package com.fastscraping.model

import spray.json.DefaultJsonProtocol._
import spray.json.{DefaultJsonProtocol, JsonFormat}

case class UniqueTag(selector: String, text: Option[String], contextElement: Option[Element] = None)

object UniqueTag {
  implicit val sprayJsonFmt: JsonFormat[UniqueTag] = jsonFormat3(UniqueTag.apply)

}
