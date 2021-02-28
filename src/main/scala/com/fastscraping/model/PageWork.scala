package com.fastscraping.model

import com.fastscraping.pagenavigation.ActionsAndScrape
import spray.json.DefaultJsonProtocol._
import spray.json.JsonFormat

case class PageWork(actionsAndScrapeData: ActionsAndScrape, contextElement: Option[Element] = None)

object PageWork {
  implicit val sprayJsonFmt: JsonFormat[PageWork] = jsonFormat2(PageWork.apply)

}
