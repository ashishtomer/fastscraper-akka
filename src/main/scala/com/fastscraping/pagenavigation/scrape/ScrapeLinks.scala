package com.fastscraping.pagenavigation.scrape

import java.util

import com.fastscraping.data.Database
import com.fastscraping.model.Element
import com.fastscraping.pagenavigation.scrape.ScrapeLinks.LinkWithText
import com.fastscraping.pagenavigation.scrape.ScrapeType.SCRAPE_LINKS
import com.fastscraping.pagenavigation.selenium.ElementFinder.FindElementBy
import com.fastscraping.pagenavigation.selenium.PageReader
import com.fastscraping.utils.Miscellaneous
import spray.json.{DefaultJsonProtocol, JsValue, RootJsonFormat}
import DefaultJsonProtocol._

import scala.jdk.CollectionConverters._

case class ScrapeLinks(linkMatch: Option[String] = Some(".+"),
                       index: String,
                       doScrollDown: Option[Boolean] = None,
                       scrollRetries: Option[Int] = None) extends Scraping {

  require(linkMatch.isDefined)

  val name = SCRAPE_LINKS

  override def collectionName(jobId: Option[String]) = Miscellaneous.CollectionByJobId(jobId, index)

  override def scrape(jobId: Option[String])(
    implicit pageReader: PageReader,
    database: Database,
    contextElement: Option[Element]) = Miscellaneous.PrintMetric("scraping links") {
    WithScroll {

      val matchedLinks = pageReader.findElements(FindElementBy.CSS_SELECTOR, "a")
        .filter(ele => Option(ele.getAttribute("href")).exists(_.matches(linkMatch.get)))
        .map(linkElement => LinkWithText(linkElement.getAttribute("href"), linkElement.getText))

      val linksJson = Map("links_to_scrape" -> matchedLinks.map(_.asJavaMap))

      Seq(PageData(collectionName(jobId), linksJson))
    }
  }

  def saveScrapedData(pageData: Seq[PageData])(implicit database: Database): Unit = {
    pageData
      .groupBy(_.collection)
      .foreach {
        case (collection, data) =>
          val docs = data.map(_.doc)
          database.saveDocuments(collection, docs)
      }
  }

  override def toJson: JsValue = ScrapeLinks.sprayJsonFmt.write(this)
}

object ScrapeLinks {
  implicit val sprayJsonFmt: RootJsonFormat[ScrapeLinks] = jsonFormat4(ScrapeLinks.apply)

  def apply(linkMatch: String, indexName: String): ScrapeLinks = new ScrapeLinks(Some(linkMatch), indexName)

  case class LinkWithText(link: String, text: String) {
    def asMap: Map[String, AnyRef] = Map("link" -> link, "text" -> text)

    def asJavaMap: util.Map[String, AnyRef] = asMap.asJava
  }

  object LinkWithText {
    implicit val sprayJsonFmt: RootJsonFormat[LinkWithText] = jsonFormat2(LinkWithText.apply)
  }

}