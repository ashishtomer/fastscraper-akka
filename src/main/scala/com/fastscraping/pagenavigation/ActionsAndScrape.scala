package com.fastscraping.pagenavigation

import com.fastscraping.pagenavigation.action.Action
import com.fastscraping.pagenavigation.scrape.Scraping
import com.fastscraping.pagenavigation.selenium.PageReader
import spray.json
import spray.json.{JsValue, RootJsonFormat}

import scala.util.{Failure, Success, Try}

trait ActionsAndScrape {
  def name: String

  def scrollDown(pageReader: PageReader): AnyRef = {
    pageReader.executeScript("window.scrollTo(0, document.body.scrollHeight)")
  }

  def toJson: JsValue
}

object ActionsAndScrape {

  implicit val sprayJsonFmt = new RootJsonFormat[ActionsAndScrape]{
    override def write(obj: ActionsAndScrape): JsValue = obj.toJson


    override def read(js: json.JsValue): ActionsAndScrape = {
      Try(js.convertTo[Action]) match {
        case Success(action) => action
        case Failure(exception) => js.convertTo[Scraping]
      }
    }
  }




}
