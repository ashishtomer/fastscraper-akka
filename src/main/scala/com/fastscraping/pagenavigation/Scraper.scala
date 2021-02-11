package com.fastscraping.pagenavigation

import com.fastscraping.data.Database
import com.fastscraping.model.ScrapeDataTypes._
import com.fastscraping.model.{DataToExtract, Element}
import com.fastscraping.pagenavigation.selenium.PageReader

class Scraper(pageReader: PageReader, database: Database) {
  def scrape(selector: String, dataToExtract: DataToExtract)(implicit contextElement: Option[Element]): Option[Unit] = {
    pageReader.findElementByCssSelector(selector).map { webElement =>
      dataToExtract.dataType match {
        case TEXT => database.saveText("youtube", pageReader.getCurrentUrl, dataToExtract.storageKey, webElement.getText)
        case IMAGE =>
      }
    }
  }
}

object Scraper {
  def apply(pageReader: PageReader, database: Database): Scraper = new Scraper(pageReader, database)
}