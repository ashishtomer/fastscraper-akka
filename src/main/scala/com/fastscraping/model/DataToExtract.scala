package com.fastscraping.model

import com.fastscraping.model.ScrapeDataTypes.ScrapeDataType
import spray.json.DefaultJsonProtocol._
import spray.json.RootJsonFormat

case class DataToExtract(storageKey: String, dataType: ScrapeDataType)

object DataToExtract {
  implicit val sprayJsonFmt: RootJsonFormat[DataToExtract] = jsonFormat2(DataToExtract.apply)
}
