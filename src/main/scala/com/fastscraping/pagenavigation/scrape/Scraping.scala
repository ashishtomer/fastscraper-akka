package com.fastscraping.pagenavigation.scrape

import com.fastscraping.data.Database
import com.fastscraping.model.Element
import com.fastscraping.pagenavigation.scrape.ScrapeType.ScrapeType
import com.fastscraping.pagenavigation.selenium.PageReader
import com.fastscraping.pagenavigation.{ActionPerformer, ActionsAndScrape}
import com.fastscraping.utils.{ElementNotFoundException, JsonParsingException, JsonWriteException}
import play.api.libs.json._

import scala.util.control.NonFatal

trait Scraping extends ActionsAndScrape {
  def scrapeType: ScrapeType

  def indexName(jobId: Option[String] = None): String

  def scrape(jobId: Option[String])(
    implicit pageReader: PageReader,
    database: Database,
    contextElement: Option[Element]): Scraping

  def actionPerformer(pageReader: PageReader) = ActionPerformer(pageReader)

  def doScrollDown: Option[Boolean]

  /**
   * Number of retries to do. If the number is 0 no retry. If number is any number less than 0 then infinite scrolling
   * @return
   */
  def scrollRetries: Option[Int]

  @throws[ElementNotFoundException]
  def WithScroll[T](f: => T)(implicit pageReader: PageReader): T = {
    var retriesAttempted: Long = 0L
    val retriesToDo = scrollRetries.getOrElse(5)

    println("Will do scrolling")

    def startScroll = {
      try (f) catch {
        case x: T => x

        case NonFatal(ex: ElementNotFoundException)
          if doScrollDown.getOrElse(false) && (retriesToDo < 0 || retriesAttempted < retriesToDo) =>

          scrollDown(pageReader)
          actionPerformer(pageReader).pause(1000)
          retriesAttempted += 1
          WithScroll(f)

        case NonFatal(ex: Throwable) =>
          println("Scrolling is disabled. Not scrolling")
          throw ex
      }
    }

    startScroll
  }
}

object Scraping {

  implicit val reads: Reads[Scraping] = (json: JsValue) => try {
    JsSuccess {
      val jsonFields = json.as[JsObject].keys
      if (jsonFields.contains("selector") && jsonFields.contains("dataToExtract")) {
        json.as[ScrapeWithSelector]
      } else if (jsonFields.contains("linkMatch")) {
        json.as[ScrapeLinks]
      } else {
        throw JsonParsingException("Scraping couldn't be parsed", Some(Json.prettyPrint(json)))
      }
    }
  } catch {
    case ex: JsonParsingException if ex.getMessage.contains("Scraping couldn't be parsed") => JsError()
  }

  implicit val writes: Writes[Scraping] = {
    case s: ScrapeWithSelector => Json.toJson(s)
    case s: ScrapeLinks => Json.toJson(s)
    case o => throw JsonWriteException(s"$o is not an instance of ${Scraping.getClass}")
  }
}
