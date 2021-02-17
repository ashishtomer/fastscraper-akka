package com.fastscraping.pagenavigation.scrape

import java.util

import com.fastscraping.data.Database
import com.fastscraping.model.Element
import com.fastscraping.pagenavigation.scrape.ScrapeLinks.LinkWithText
import com.fastscraping.pagenavigation.scrape.ScrapeType.SCRAPE_LINKS
import com.fastscraping.pagenavigation.selenium.PageReader
import com.fastscraping.utils.Miscellaneous
import play.api.libs.json.{Format, Json}

import scala.jdk.CollectionConverters._

case class ScrapeLinks(linkMatch: Option[String] = Some(".+"),
                       index: String,
                       doScrollDown: Option[Boolean] = None,
                       scrollRetries: Option[Int] = None) extends Scraping {

  require(linkMatch.isDefined)

  val scrapeType = SCRAPE_LINKS

  override def indexName(jobId: Option[String]) = Miscellaneous.CollectionByJobId(jobId, index)

  override def scrape(jobId: Option[String])(
    implicit pageReader: PageReader,
    database: Database,
    contextElement: Option[Element]): Scraping = WithScroll {

    println(s"[page=${pageReader.getCurrentUrl}] Will scrape links with ${linkMatch} match and save in ${indexName(jobId)}")
    val matchedLinks = pageReader.findElementsByCssSelector("a")
      .filter(ele => Option(ele.getAttribute("href")).exists(_.matches(linkMatch.get)))
      .map(linkElement => LinkWithText(linkElement.getAttribute("href"), linkElement.getText))

    val linksJson = Map("links_to_scrape" -> matchedLinks.map(_.asJavaMap))

    database.saveDocument(indexName(jobId), pageReader.getCurrentUrl, linksJson)
    this
  }
}

object ScrapeLinks {
  implicit val fmt: Format[ScrapeLinks] = Json.format[ScrapeLinks]

  def apply(linkMatch: String, indexName: String): ScrapeLinks = new ScrapeLinks(Some(linkMatch), indexName)

  case class LinkWithText(link: String, text: String) {
    def asMap: Map[String, AnyRef] = Map("link" -> link, "text" -> text)

    def asJavaMap: util.Map[String, AnyRef] = asMap.asJava
  }

  object LinkWithText {
    implicit val fmt: Format[LinkWithText] = Json.format[LinkWithText]
  }

}