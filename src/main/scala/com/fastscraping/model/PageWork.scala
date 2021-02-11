package com.fastscraping.model

import com.fastscraping.pagenavigation.ActionsAndScrapeData
import play.api.libs.json.Json

case class PageWork(actionsAndScrapeData: ActionsAndScrapeData, contextElement: Option[Element] = None)

object PageWork {
  implicit lazy val fmt = Json.format[PageWork]
}
