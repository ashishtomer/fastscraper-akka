package com.fastscraping.pagenavigation.scrape
import com.fastscraping.data.Database
import com.fastscraping.model.Element
import com.fastscraping.pagenavigation.selenium.ElementFinder.FindElementBy.FindElementBy
import com.fastscraping.pagenavigation.selenium.PageReader
import com.fastscraping.utils.Miscellaneous
import spray.json.{JsObject, JsValue}

case class ScrapeTable(by: FindElementBy,
                       value: String,
                       index: String,
                       doScrollDown: Option[Boolean] = None,
                       scrollRetries: Option[Int] = None) extends Scraping {

  override def collectionName(jobId: Option[String]): String = {
    Miscellaneous.CollectionByJobId(jobId, index)
  }

  override def scrape(jobId: Option[String])(
    implicit pageReader: PageReader,
    database: Database,
    contextElement: Option[Element]): Seq[PageData] = {
    Seq.empty[PageData]
  }

  override def name: String = "SCRAPE_TABLE"

  override def toJson: JsValue = JsObject.empty
}
