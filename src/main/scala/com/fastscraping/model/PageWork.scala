package com.fastscraping.model

import com.fastscraping.pagenavigation.ActionsAndScrape
import play.api.libs.json.Json

case class PageWork(actionsAndScrapeData: ActionsAndScrape, contextElement: Option[Element] = None)

object PageWork {
  implicit lazy val fmt = Json.format[PageWork]
}
