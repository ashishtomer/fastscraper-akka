package com.fastscraping.pagenavigation.scrape

import com.fastscraping.data.Database
import com.fastscraping.model.ScrapeDataTypes.TEXT
import com.fastscraping.model.{DataToExtract, Element}
import com.fastscraping.pagenavigation.selenium.ElementFinder.FindElementBy.FindElementBy
import com.fastscraping.pagenavigation.selenium.PageReader
import com.fastscraping.utils.Miscellaneous
import play.api.libs.json.Json

case class ScrapeData(by: FindElementBy,
                      value: String,
                      dataToExtract: DataToExtract,
                      index: String,
                      doScrollDown: Option[Boolean] = None,
                      scrollRetries: Option[Int] = None) extends Scraping {

  val name = ScrapeType.SCRAPE_TEXT_WITH_SELECTOR

  override def collectionName(jobId: Option[String]): String = Miscellaneous.CollectionByJobId(jobId, index)

  def scrape(jobId: Option[String])(
    implicit pageReader: PageReader,
    database: Database,
    contextElement: Option[Element]) =
    Miscellaneous.PrintMetric("scraping data") {
      WithScroll {
        pageReader.findElement(by, value) match {
          case Some(webElement) =>
            dataToExtract.dataType match {
              case TEXT =>
                Seq(PageData(collectionName(jobId), Map(dataToExtract.storageKey -> webElement.getText)))

              //TODO: Add support for image scraping
              //case IMAGE =>
            }

          case None =>
            val err = s"[$by=$value]' not found on ${pageReader.getCurrentUrl} to scrape in context=$contextElement"
            logger.error(err)
            Seq()
        }
      }
    }
}

object ScrapeData {
  implicit val fmt = Json.format[ScrapeData]
}
