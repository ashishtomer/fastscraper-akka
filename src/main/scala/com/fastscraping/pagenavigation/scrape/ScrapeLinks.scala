package com.fastscraping.pagenavigation.scrape

import com.fastscraping.data.Database
import com.fastscraping.model.Element
import com.fastscraping.pagenavigation.scrape.ScrapeLinks.LinkWithText
import com.fastscraping.pagenavigation.selenium.PageReader
import org.mongodb.scala.bson.collection.immutable.Document
import play.api.libs.json.{Format, JsObject, Json}
import ScrapeType.SCRAPE_LINKS

case class ScrapeLinks(linkMatch: Option[String] = Some(".+"), indexName: String) extends Scraping {
  require(linkMatch.isDefined)

  val scrapeType = SCRAPE_LINKS

  override def scrape(implicit pageReader: PageReader, database: Database, contextElement: Option[Element]): Scraping = {
    val matchedLinks = pageReader.findElementsByCssSelector("a")
      .filter(ele => Option(ele.getAttribute("href")).exists(_.matches(linkMatch.get)))
      .map(linkElement => LinkWithText(linkElement.getAttribute("href"), linkElement.getText))

    val linksJson = JsObject(Map("links_to_scrape" -> Json.toJson(matchedLinks))).toString()

    database.saveDocument(indexName, pageReader.getCurrentUrl, Document(linksJson))
    this
  }
}

object ScrapeLinks {
  implicit val fmt: Format[ScrapeLinks] = Json.format[ScrapeLinks]

  def apply(linkMatch: String, indexName: String): ScrapeLinks = new ScrapeLinks(Some(linkMatch), indexName)

  case class LinkWithText(link: String, text: String)

  object LinkWithText {
    implicit val fmt: Format[LinkWithText] = Json.format[LinkWithText]
  }

}