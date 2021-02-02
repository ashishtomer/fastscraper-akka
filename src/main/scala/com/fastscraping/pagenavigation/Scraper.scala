package com.fastscraping.pagenavigation

import com.fastscraping.data.Database
import com.fastscraping.model.ScrapeDataTypes._
import com.fastscraping.model.DataToExtract
import com.fastscraping.pagenavigation.selenium.PageReader

class Scraper(pageReader: PageReader, database: Database) {
  def scrape(selector: String, dataToExtract: DataToExtract): Option[Unit] = {
    pageReader.findElementByCssSelector(selector).map { webElement =>
      dataToExtract.dataType match {
        case TEXT => database.saveText(dataToExtract.storageKey, webElement.getText)
        case IMAGE =>
      }
    }
  }
}
