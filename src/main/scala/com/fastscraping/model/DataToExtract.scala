package com.fastscraping.model

import com.fastscraping.model.ScrapeDataTypes.ScrapeDataType
import play.api.libs.json.Json

case class DataToExtract(storageKey: String, dataType: ScrapeDataType)

object DataToExtract {
  implicit val fmt = Json.format[DataToExtract]
}
