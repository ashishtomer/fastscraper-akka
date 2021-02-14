package com.fastscraping.pagenavigation.scrape

import com.fastscraping.data.Database
import com.fastscraping.model.Element
import com.fastscraping.pagenavigation.scrape.ScrapeCrawlLinks.ScrapeLinksBy
import com.fastscraping.pagenavigation.scrape.ScrapeType.{SCRAPE_CRAWL_LINKS, ScrapeType}
import com.fastscraping.pagenavigation.selenium.PageReader
import com.fastscraping.utils.Miscellaneous
import com.fastscraping.utils.Miscellaneous.CRAWL_LINK_COLLECTION
import org.mongodb.scala.bson.collection.immutable.Document
import org.openqa.selenium.WebElement
import play.api.libs.json.{JsFalse, JsObject, JsString}

import scala.util.{Failure, Success, Try}

case class ScrapeCrawlLinks(findLinksBy: String, value: String) extends Scraping {
  override def scrapeType: ScrapeType = SCRAPE_CRAWL_LINKS

  override def indexName: String = CRAWL_LINK_COLLECTION

  override def scrape(implicit pageReader: PageReader, database: Database, contextElement: Option[Element]): Scraping = {
    getLinkElements()
      .foreach { linkElement =>
        val link = linkElement.getAttribute("href")
        val json = JsObject(Seq(
          Miscellaneous.CRAWL_LINK_INDEX -> JsString(link),
          Miscellaneous.IS_CRAWLED -> JsFalse
        ))

        Try(database.saveDocument(indexName, pageReader.getCurrentUrl, Document(json.toString))) match {
          case Success(x) => x
          case Failure(ex) if (ex.getMessage.contains("duplicate key error collection")) =>
            println(s"The [link=$link] already saved in mongo")
          case Failure(ex) => throw ex
        }
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
