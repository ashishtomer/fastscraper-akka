package com.fastscraping.pagenavigation.scrape

import com.fastscraping.data.Database
import com.fastscraping.model.ScrapeDataTypes.{IMAGE, TEXT}
import com.fastscraping.model.{DataToExtract, Element}
import com.fastscraping.pagenavigation.selenium.PageReader
import com.fastscraping.utils.ElementNotFoundException
import play.api.libs.json.Json

case class ScrapeWithSelector(selector: String, dataToExtract: DataToExtract, indexName: String) extends Scraping {
  val scrapeType = ScrapeType.SCRAPE_TEXT_WITH_SELECTOR

  def scrape(implicit pageReader: PageReader, database: Database, contextElement: Option[Element]): Scraping = {
    synchronized {
      pageReader.findElementByCssSelector(selector) match {
        case Some(webElement) =>
          dataToExtract.dataType match {
            case TEXT =>
              database.saveText(indexName, pageReader.getCurrentUrl, dataToExtract.storageKey, webElement.getText)
            case IMAGE =>
          }

        case None =>
          val err = s"'$selector' not found on ${pageReader.getCurrentUrl} to scrape in context=$contextElement"
          throw ElementNotFoundException(err)
      }

      this
    }
  }

}

object ScrapeWithSelector {
  implicit val fmt = Json.format[ScrapeWithSelector]
}
