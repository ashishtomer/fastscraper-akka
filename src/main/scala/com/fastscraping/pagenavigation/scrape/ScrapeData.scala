package com.fastscraping.pagenavigation.scrape

import com.fastscraping.model.DataToExtract
import com.fastscraping.pagenavigation.{ActionPerformer, ActionsAndScrapeData, Scraper}
import play.api.libs.json.Json

case class ScrapeData(selector: String, dataToExtract: DataToExtract) extends ActionsAndScrapeData {
  def extractData(scraper: Scraper) = scraper.scrape(selector, dataToExtract)
}

object ScrapeData {
  implicit val fmt = Json.format[ScrapeData]
}
