package com.fastscraping.model

import com.fastscraping.model.actions.Actions
import com.fastscraping.utils.{JsonParsingException, JsonWriteException}
import play.api.libs.json._

trait ActionsAndScrapeData

object ActionsAndScrapeData {
  private val reads: Reads[ActionsAndScrapeData] = (json: JsValue) => {
    json.asOpt[Actions] match {
      case Some(actions) => JsSuccess(actions)
      case None =>
        json.asOpt[ScrapeData] match {
          case Some(scrapeData) => JsSuccess(scrapeData)
          case None =>
            throw JsonParsingException("Could not parse json to either of Actions and ScrapeData", Some(json.toString))
        }
    }
  }

  private val writes: Writes[ActionsAndScrapeData] = {
    case actions: Actions => Json.toJson(actions)
    case scrapeData: ScrapeData => Json.toJson(scrapeData)
    case obj: Any =>
      throw JsonWriteException(s"${obj.getClass} not instance of ${ActionsAndScrapeData.getClass}. Can't serialize.")
  }

  implicit val fmt: Format[ActionsAndScrapeData] = Format(reads, writes)
}
