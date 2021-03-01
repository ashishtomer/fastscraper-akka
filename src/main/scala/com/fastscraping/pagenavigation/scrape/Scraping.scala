package com.fastscraping.pagenavigation.scrape

import com.fastscraping.data.Database
import com.fastscraping.model.Element
import com.fastscraping.pagenavigation.ActionsAndScrape
import com.fastscraping.pagenavigation.action.ActionPerformer
import com.fastscraping.pagenavigation.selenium.PageReader
import com.fastscraping.utils.{ElementNotFoundException, FsLogging, JsonParsingException, JsonWriteException}
import spray.json.{JsValue, RootJsonFormat}

import scala.util.control.NonFatal

trait Scraping extends ActionsAndScrape with FsLogging {

  def collectionName(jobId: Option[String] = None): String

  def scrape(jobId: Option[String])(
    implicit pageReader: PageReader,
    database: Database,
    contextElement: Option[Element]): Seq[PageData]

  def actionPerformer(pageReader: PageReader) = ActionPerformer(pageReader)

  def doScrollDown: Option[Boolean]

  /**
   * Number of retries to do. If the number is 0 no retry. If number is any number less than 0 then infinite scrolling
   *
   * @return
   */
  def scrollRetries: Option[Int]

  @throws[ElementNotFoundException]
  def WithScroll[T](f: => T)(implicit pageReader: PageReader): T = {
    var retriesAttempted: Long = 0L
    val retriesToDo = scrollRetries.getOrElse(5)

    def startScroll = {
      try (f) catch {
        case NonFatal(ex: ElementNotFoundException)
          if doScrollDown.getOrElse(false) && (retriesToDo < 0 || retriesAttempted < retriesToDo) =>

          scrollDown(pageReader)
          retriesAttempted += 1
          WithScroll(f)

        case NonFatal(ex: Throwable) => throw ex
      }
    }

    startScroll
  }
}

object Scraping extends FsLogging {
  implicit val sprayJsonFmt = new RootJsonFormat[Scraping] {
    override def read(json: JsValue): Scraping = {
      val jsonFields = json.asJsObject.fields
      if (jsonFields.contains("by") && jsonFields.contains("dataToExtract")) {
        json.convertTo[ScrapeData]
      } else if (jsonFields.contains("withAction")) {
        json.convertTo[NextPage]
      } else if (jsonFields.contains("findCrawlLinksBy") && jsonFields.contains("value")) {
        json.convertTo[ScrapeCrawlLinks]
      } else if (jsonFields.contains("linkMatch")) {
        json.convertTo[ScrapeLinks]
      } else {
        throw JsonParsingException("Scraping couldn't be parsed", Some(json.prettyPrint))
      }
    }

    override def write(obj: Scraping): JsValue = obj match {
      case s: ScrapeData => s.toJson
      case s: ScrapeLinks => s.toJson
      case s: ScrapeCrawlLinks => s.toJson
      case s: NextPage => s.toJson
      case o => throw JsonWriteException(s"$o is not an instance of ${Scraping.getClass}")
    }
  }
}
