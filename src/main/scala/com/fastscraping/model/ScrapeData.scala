package com.fastscraping.model

import play.api.libs.json.Json

case class ScrapeData(selector: String, dataToExtract: DataToExtract) extends ActionsAndScrapeData

object ScrapeData {
  implicit val fmt = Json.format[ScrapeData]
}
