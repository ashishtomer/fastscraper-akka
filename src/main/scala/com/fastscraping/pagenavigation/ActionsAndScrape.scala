package com.fastscraping.pagenavigation

import com.fastscraping.pagenavigation.action.Actions
import com.fastscraping.pagenavigation.scrape.Scraping
import com.fastscraping.pagenavigation.selenium.PageReader
import com.fastscraping.utils.{JsonParsingException, JsonWriteException}
import play.api.libs.json._

trait ActionsAndScrape {
  def scrollDown(pageReader: PageReader): AnyRef = {
    pageReader.executeScript("window.scrollTo(0, document.body.scrollHeight)");
  }
}

object ActionsAndScrape {
  private val reads: Reads[ActionsAndScrape] = (json: JsValue) => {
    json.asOpt[Actions] match {
      case Some(actions) => JsSuccess(actions)
      case None =>
        json.asOpt[Scraping] match {
          case Some(scrapeData) => JsSuccess(scrapeData)
          case None =>
            throw JsonParsingException("Could not parse json to either of Actions and ScrapeData", Some(json.toString))
        }
    }
  }

  private val writes: Writes[ActionsAndScrape] = {
    case actions: Actions => Json.toJson(actions)
    case scraping: Scraping => Json.toJson(scraping)
    case obj: Any =>
      throw JsonWriteException(s"${obj.getClass} not instance of ${ActionsAndScrape.getClass}. Can't serialize.")
  }

  implicit val fmt: Format[ActionsAndScrape] = Format(reads, writes)
}
