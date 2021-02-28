package com.fastscraping.pagenavigation.scrape

import com.fastscraping.data.Database
import com.fastscraping.model.Element
import com.fastscraping.pagenavigation.scrape.ScrapeCrawlLinks.ScrapeLinksBy
import com.fastscraping.pagenavigation.scrape.ScrapeCrawlLinks.ScrapeLinksBy.ScrapeLinksBy
import com.fastscraping.pagenavigation.scrape.ScrapeType.SCRAPE_CRAWL_LINKS
import com.fastscraping.pagenavigation.selenium.ElementFinder.FindElementBy
import com.fastscraping.pagenavigation.selenium.PageReader
import com.fastscraping.utils.EnumSprayJsonFormat
import com.fastscraping.utils.Miscellaneous._
import org.openqa.selenium.WebElement
import spray.json.DefaultJsonProtocol._
import spray.json.JsValue

case class ScrapeCrawlLinks(findCrawlLinksBy: ScrapeLinksBy,
                            value: String,
                            cleanseLink: Option[CleanseLink] = None,
                            doScrollDown: Option[Boolean] = None,
                            scrollRetries: Option[Int] = None) extends Scraping {

  override def name: String = SCRAPE_CRAWL_LINKS

  override def collectionName(jobId: Option[String]): String = CrawlLinkCollection(jobId)

  override def scrape(jobId: Option[String])(
    implicit pageReader: PageReader,
    database: Database,
    contextElement: Option[Element]): Seq[PageData] = {
    PrintMetric(s"scraping crawl links [$findCrawlLinksBy=$value][page=${pageReader.currentUrl}]") {
      WithScroll {
        val collection = collectionName(jobId)
        val linksToCrawl = getLinkElements()
          .flatMap { linkElement =>
            val link = linkElement.getAttribute("href")
            val cleansedLink = cleanseLink.map(_.cleanse(link)).getOrElse(link)

            if (!database.isLinkScraped(jobId, cleansedLink)) {
              val document: Map[String, AnyRef] = Map(
                _LINK_TO_CRAWL -> cleansedLink.asInstanceOf[AnyRef],
                "_id" -> cleansedLink.asInstanceOf[AnyRef],
                IS_CRAWLED -> false.asInstanceOf[AnyRef]
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
  }

  def saveScrapedData(pageData: Seq[PageData])(implicit pageReader: PageReader, database: Database): Unit = {
    pageData.foreach { data =>
      database.saveDocument(data.collection, pageReader.currentUrl, data.doc)
    }
  }

  private def getLinkElements()(implicit pageReader: PageReader, contextElement: Option[Element]): Seq[WebElement] = {
    import ScrapeLinksBy._
    findCrawlLinksBy match {
      case BY_TEXT => pageReader.findElements(FindElementBy.LINK_TEXT, value)
      case BY_SELECTOR => pageReader.findElements(FindElementBy.CSS_SELECTOR, value)
      case BY_PARTIAL_TEXT => pageReader.findElements(FindElementBy.PARTIAL_LINK_TEXT, value)
      case BY_REGEX => pageReader.findElements(FindElementBy.CSS_SELECTOR, "a")
        .filter(ele => Option(ele.getAttribute("href")).exists(_.matches(value)))
    }
  }

  override def toJson: JsValue = ScrapeCrawlLinks.sprayJsonFmt.write(this)
}

object ScrapeCrawlLinks {

  object ScrapeLinksBy extends Enumeration {
    type ScrapeLinksBy = Value
    val BY_SELECTOR, BY_REGEX, BY_TEXT, BY_PARTIAL_TEXT = Value
    implicit val sprayJsonFmt = EnumSprayJsonFormat(ScrapeLinksBy)
  }

  implicit val sprayJsonFmt = jsonFormat(ScrapeCrawlLinks.apply, "findCrawlLinksBy",
    "value",
    "cleanseLink",
    "doScrollDown",
    "scrollRetries")
}
