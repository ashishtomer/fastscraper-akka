package com.fastscraping.pagenavigation.selenium

import com.fastscraping.data.Database
import com.fastscraping.model.{Element, PageWork, WebpageIdentifier}
import com.fastscraping.pagenavigation.ActionPerformer
import com.fastscraping.pagenavigation.action.Actions
import com.fastscraping.pagenavigation.scrape.{PageData, Scraping}
import com.fastscraping.utils.Miscellaneous._
import com.fastscraping.utils.{FsLogging, MultipleMatchingIdentifiersException}
import org.bson.Document
import org.openqa.selenium.WebElement

import scala.collection.immutable
import scala.collection.mutable.ListBuffer
import scala.concurrent.{ExecutionContext, Future}

class ScrapeJobExecutor(implicit pageReader: PageReader, db: Database) extends FsLogging {

  private val actionPerformer = ActionPerformer(pageReader)

  def execute(link: String, webpageIdentifiers: ListBuffer[WebpageIdentifier], jobId: Option[String])(implicit ec: ExecutionContext) = {

    pageReader.get(link)

    filterPageModifier(webpageIdentifiers) map {
      case Some(webpageIdentifier) => PrintMetric("performing operations") {
        performOperations(jobId, webpageIdentifier)
      }
      case None => logger.warn(s"No webpage identifier matched with ${pageReader.getCurrentUrl}")
    } map (_ => db.markLinkAsScraped(jobId, link))
  }

  private def performOperations(jobId: Option[String], webpageIdentifier: WebpageIdentifier): immutable.Iterable[Unit] = {
    val scrapedData = scala.collection.mutable.ListBuffer[PageData]()

    webpageIdentifier.pageWorks
      .map( pageWork => {
        implicit val ce = pageWork.contextElement
        pageWork match {
          case PageWork(actions: Actions, _) => actions.perform(actionPerformer)
          case PageWork(scraping: Scraping, _) => scrapedData.prependAll(scraping.scrape(jobId))
        }
      })
      .filter(_.isInstanceOf[Seq[PageData]])

    scrapedData.groupBy(_.collection)
      .map {
        case (collection: String, data: Seq[PageData]) =>
          val mongoDoc = new Document()
          data.foreach(_.doc.foreach(keyVal => mongoDoc.append(keyVal._1, keyVal._2)))
          db.saveDocument(collection, pageReader.getCurrentUrl, mongoDoc)
      }
  }

  private def filterPageModifier[M](webpageIdentifiers: ListBuffer[WebpageIdentifier])(
    implicit ec: ExecutionContext): Future[Option[WebpageIdentifier]] = PrintMetric("filtering page modifier") {

    implicit val contextElement: Option[Element] = None

    def filter(pageIdentifier: WebpageIdentifier): Future[(Int, WebpageIdentifier)] = PrintMetric("running filter") {
      {
        import pageIdentifier.pageUniqueness._

        def urlRegexMatched() = Future(PrintMetric("matching url") {
          logger.info(s"filter(): match-regex=$urlRegex [pageIdentifier= $pageIdentifier]")
          pageReader.getCurrentUrl matches urlRegex
        })

        def anyUniqueTagNotFound() = Future {
          PrintMetric("finding unique tag") {
            uniqueTags.exists { uniqueTag =>
              logger.info(s"filter(): $uniqueTag [pageIdentifier= $pageIdentifier]")

              def tagTextExists(tagFound: WebElement): Boolean = {
                val uniqueTagText = uniqueTag.text.getOrElse("")
                if (uniqueTagText.length == 0) true
                else tagFound.getText != null && tagFound.getText.contains(uniqueTagText)
              }

              val tagFoundOpt = pageReader.findElementByCssSelector(uniqueTag.selector)(uniqueTag.contextElement)
              tagFoundOpt.isEmpty || !tagTextExists(tagFoundOpt.get)
            }
          }
        }

        def uniqueStringNotExists() = Future {
          PrintMetric("finding unique string") {
            uniqueStrings.exists { uniqueString =>
              logger.info(s"filter(): $uniqueString [pageIdentifier=$pageIdentifier]")
              val stringContextOpt = pageReader.findElementByTagName("body")(uniqueString.contextElement)
              stringContextOpt.isEmpty ||
                stringContextOpt.get.getText == null ||
                !stringContextOpt.get.getText.contains(uniqueString.string)
            }
          }
        }

        Future.sequence(Seq(urlRegexMatched(), anyUniqueTagNotFound(), uniqueStringNotExists()))
          .map {
            case Seq(regexMatched, uniqueTagMatched, stringNotExists) =>
              val regexScore = if (regexMatched) 47 else 0
              val tagScore = if (uniqueTagMatched) 0 else 13
              val stringScore = if (stringNotExists) 0 else 2

              logger.info(s"regexMatched=$regexMatched uniqueTagMatched=$uniqueTagMatched stringNotExists=$stringNotExists identifier=$pageIdentifier")
              logger.info(s"regexScore=$regexScore tagScore=$tagScore stringScore=$stringScore identifier=$pageIdentifier")

              (regexScore + tagScore + stringScore, pageIdentifier)
          }
      }
    }

    RunFuturesInParallel(webpageIdentifiers.map(x => filter(x)).toSeq)
      .map { identifierWithScore =>
        val sorted = identifierWithScore.sortWith((first, seconds) => first._1 > seconds._1)

        logger.debug(s"Before sorting: $webpageIdentifiers")
        webpageIdentifiers.clear()
        val sortedIdentifiers = webpageIdentifiers.appendAll(sorted.map(_._2))
        logger.debug(s"After sorting: $sortedIdentifiers")

        if (sorted.head._1 < ScrapeJobExecutor.UrlRegexMatchScore) {
          None
        } else if (sorted.size > 1 && sorted.head._1 == sorted.tail.head._1) {
          throw MultipleMatchingIdentifiersException(s"Two page identifiers matched for ${pageReader.getCurrentUrl}. $sorted")
        } else {
          Some(sorted.head._2)
        }

      }
  }
}

object ScrapeJobExecutor {
  def apply()(implicit pageReader: PageReader, db: Database): ScrapeJobExecutor = new ScrapeJobExecutor()

  val UrlRegexMatchScore = 47
  val UniqueTagMatchScore = 13
  val UniqueStringMatchScore = 2
}
