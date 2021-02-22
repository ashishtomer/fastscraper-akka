package com.fastscraping.pagenavigation.scrape

import com.fastscraping.data.Database
import com.fastscraping.model.Element
import com.fastscraping.pagenavigation.scrape.ScrapeCrawlLinks.ScrapeLinksBy
import com.fastscraping.pagenavigation.scrape.ScrapeType.SCRAPE_CRAWL_LINKS
import com.fastscraping.pagenavigation.selenium.ElementFinder.FindElementBy
import com.fastscraping.pagenavigation.selenium.PageReader
import com.fastscraping.utils.Miscellaneous
import com.fastscraping.utils.Miscellaneous.CrawlLinkCollection
import org.openqa.selenium.WebElement

case class ScrapeCrawlLinks(findLinksBy: String,
                            value: String,
                            doScrollDown: Option[Boolean] = None,
                            scrollRetries: Option[Int] = None) extends Scraping {

  override def name: String = SCRAPE_CRAWL_LINKS

  override def collectionName(jobId: Option[String]): String = CrawlLinkCollection(jobId)

  override def scrape(jobId: Option[String])(
    implicit pageReader: PageReader,
    database: Database,
    contextElement: Option[Element]): Seq[PageData] = Miscellaneous.PrintMetric("scraping crawl links") {
    WithScroll {
      val collection = collectionName(jobId)
      val linksToCrawl = getLinkElements()
        .flatMap { linkElement =>
          val link = linkElement.getAttribute("href")
          if (!database.isLinkScraped(jobId, link)) {
            val document: Map[String, AnyRef] = Map(
              Miscellaneous._LINK_TO_CRAWL -> link.asInstanceOf[AnyRef],
              "_id" -> link.asInstanceOf[AnyRef],
              Miscellaneous.IS_CRAWLED -> false.asInstanceOf[AnyRef]
            )
            Some(PageData(collection, document))
          } else {
            None
          }
        }

      saveScrapedData(linksToCrawl)
      Seq.empty
    }
  }

  def saveScrapedData(pageData: Seq[PageData])(implicit pageReader: PageReader, database: Database): Unit = {
    pageData.foreach { data =>
      database.saveDocument(data.collection, pageReader.getCurrentUrl, data.doc)
    }
    logger.info(s"${pageData.size} link scraped from ${pageReader.getCurrentUrl}")
  }

  private def getLinkElements()(implicit pageReader: PageReader, contextElement: Option[Element]): Seq[WebElement] = {
    import ScrapeLinksBy._
    ScrapeLinksBy.withName(findLinksBy) match {
      case BY_TEXT => pageReader.findElements(FindElementBy.LINK_TEXT, value)
      case BY_SELECTOR => pageReader.findElements(FindElementBy.CSS_SELECTOR, value)
      case BY_PARTIAL_TEXT => pageReader.findElements(FindElementBy.PARTIAL_LINK_TEXT, value)
      case BY_REGEX => pageReader.findElements(FindElementBy.CSS_SELECTOR, "a")
        .filter(ele => Option(ele.getAttribute("href")).exists(_.matches(value)))
    }
  }
}

object ScrapeCrawlLinks {

  object ScrapeLinksBy extends Enumeration {
    type ScrapeLinksBy = Value
    val BY_SELECTOR, BY_REGEX, BY_TEXT, BY_PARTIAL_TEXT = Value
  }

}
