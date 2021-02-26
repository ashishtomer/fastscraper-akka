package com.fastscraping.pagenavigation.scrape
import com.fastscraping.data.Database
import com.fastscraping.model.Element
import com.fastscraping.pagenavigation.selenium.ElementFinder.FindElementBy.FindElementBy
import com.fastscraping.pagenavigation.selenium.PageReader
import com.fastscraping.utils.Miscellaneous

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
    contextElement: Option[Element]): Seq[PageData] = ???

  override def doScrollDown: Option[Boolean] = ???

  /**
   * Number of retries to do. If the number is 0 no retry. If number is any number less than 0 then infinite scrolling
   *
   * @return
   */
  override def scrollRetries: Option[Int] = ???

  override def name: String = ???
}
