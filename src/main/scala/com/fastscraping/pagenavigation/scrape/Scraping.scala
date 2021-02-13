package com.fastscraping.pagenavigation.scrape

import com.fastscraping.data.Database
import com.fastscraping.model.Element
import com.fastscraping.pagenavigation.ActionsAndScrape
import com.fastscraping.pagenavigation.selenium.PageReader
import com.fastscraping.utils.{JsonParsingException, JsonWriteException}
import play.api.libs.json._

trait Scraping extends ActionsAndScrape {
  def name: String
  def scrape(pageReader: PageReader, database: Database)(implicit contextElement: Option[Element]): Scraping
}

object Scraping {

  implicit val reads: Reads[Scraping] = (json: JsValue) => try {
    JsSuccess {
      val jsonFields = json.as[JsObject].keys
      if (jsonFields.contains("selector") && jsonFields.contains("dataToExtract")) {
        json.as[ScrapeWithSelector]
      } else if (jsonFields.contains("linkMatch")) {
        json.as[ScrapeLinks]
      } else {
        throw JsonParsingException("Scraping couldn't be parsed", Some(Json.prettyPrint(json)))
      }
    }
  } catch {
    case ex: JsonParsingException if ex.getMessage.contains("Scraping couldn't be parsed") => JsError()
  }

  implicit val writes: Writes[Scraping] = {
    case s: ScrapeWithSelector => Json.toJson(s)
    case s: ScrapeLinks => Json.toJson(s)
    case o => throw JsonWriteException(s"$o is not an instance of ${Scraping.getClass}")
  }
}
