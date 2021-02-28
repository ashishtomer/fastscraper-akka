package com.fastscraping.model

import spray.json.DefaultJsonProtocol._
import spray.json.JsonFormat

case class UniqueString(string: String, contextElement: Option[Element] = None)

object UniqueString {
  implicit val sprayJsonFmt: JsonFormat[UniqueString] = jsonFormat2(UniqueString.apply)
}
