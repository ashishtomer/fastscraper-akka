package com.fastscraping.pagenavigation.scrape

import com.fastscraping.data.Database
import com.fastscraping.model.Element
import com.fastscraping.pagenavigation.scrape.ScrapeCrawlLinks.ScrapeLinksBy
import com.fastscraping.pagenavigation.scrape.ScrapeType.{SCRAPE_CRAWL_LINKS, ScrapeType}
import com.fastscraping.pagenavigation.selenium.PageReader
import com.fastscraping.utils.Miscellaneous
import com.fastscraping.utils.Miscellaneous.CRAWL_LINK_COLLECTION
import org.openqa.selenium.WebElement

case class ScrapeCrawlLinks(findLinksBy: String, value: String) extends Scraping {
  override def scrapeType: ScrapeType = SCRAPE_CRAWL_LINKS

  override def indexName: String = CRAWL_LINK_COLLECTION

  override def scrape(implicit pageReader: PageReader, database: Database, contextElement: Option[Element]): Scraping = {
    getLinkElements()
      .foreach { linkElement =>
        val link = linkElement.getAttribute("href")
        val document: Map[String, AnyRef] = Map(
          Miscellaneous.CRAWL_LINK_INDEX -> link.asInstanceOf[AnyRef],
          Miscellaneous.IS_CRAWLED -> false.asInstanceOf[AnyRef]
        )

        database.saveDocument(indexName, pageReader.getCurrentUrl, document)
      }

    this
  }

  private def getLinkElements()(implicit pageReader: PageReader, contextElement: Option[Element]): Seq[WebElement] = {
    import ScrapeLinksBy._
    ScrapeLinksBy.withName(findLinksBy) match {
      case BY_TEXT => pageReader.findElementsByLinkText(value)
      case BY_SELECTOR => pageReader.findElementsByCssSelector(value)
      case BY_PARTIAL_TEXT => pageReader.findElementsByPartialLinkText(value)
      case BY_REGEX => pageReader.findElementsByCssSelector("a")
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
