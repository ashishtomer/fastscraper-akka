package com.fastscraping.pagenavigation.selenium

import com.fastscraping.data.Database
import com.fastscraping.model.{ActionName, Element, PageWork, WebpageIdentifier}
import com.fastscraping.pagenavigation.action.{Action, ActionPerformer, FindElementAction}
import com.fastscraping.pagenavigation.scrape.{PageData, Scraping}
import com.fastscraping.pagenavigation.selenium.ElementFinder.FindElementBy
import com.fastscraping.utils.Miscellaneous._
import com.fastscraping.utils.{FsLogging, MultipleMatchingIdentifiersException}
import org.openqa.selenium.WebElement

import scala.collection.mutable.ListBuffer
import scala.concurrent.{ExecutionContext, Future}

class ScrapeJobExecutor(implicit pageReader: PageReader, db: Database) extends FsLogging {

  private val actionPerformer = ActionPerformer(pageReader)

  def execute(link: String, webpageIdentifiers: ListBuffer[WebpageIdentifier], blockingUrl: Option[String], jobId: Option[String])(implicit ec: ExecutionContext) = {

    pageReader.get(link)

    filterPageModifier(webpageIdentifiers) map {
      case Some(webpageIdentifier) => PrintMetric("performing operations") {
        performOperations(jobId, webpageIdentifier, blockingUrl)
      }
      case None => logger.warn(s"No webpage identifier matched with ${pageReader.currentUrl}")
    } map { _ =>
      db.markLinkAsScraped(jobId, link)
      logger.info(s"Marked as scraped [link=$link]")
    } recover {
      case ex: MultipleMatchingIdentifiersException =>
      //Do nothing for now
    }
  }

  private def performOperations(jobId: Option[String], webpageIdentifier: WebpageIdentifier, blockingUrl: Option[String]) = {
    val scrapedData = scala.collection.mutable.ListBuffer[PageData]()

    webpageIdentifier.pageWorks
      .foreach(pageWork => {
        implicit val ce = pageWork.contextElement
        logger.info(s"[work=${pageWork.actionsAndScrapeData.name}] Start")
        pageWork match {
          case PageWork(actions: Action, _) => actions.perform(actionPerformer)
          case PageWork(scraping: Scraping, _) =>
            val dataFromPage = scraping.scrape(jobId)
            if (dataFromPage.nonEmpty) scrapedData.appendAll(dataFromPage)
        }
        blockingUrl.foreach(url => if (pageReader.currentUrl == url) clearCacheHistory)
        logger.info(s"[work=${pageWork.actionsAndScrapeData.name}] End")
      })

    scrapedData
      .groupBy(_.collection)
      .foreach {
        case (collection, data) => db.saveDocument(collection, pageReader.currentUrl, data.flatMap(_.doc).toMap)
      }
  }

  private def filterPageModifier[M](webpageIdentifiers: ListBuffer[WebpageIdentifier])(
    implicit ec: ExecutionContext): Future[Option[WebpageIdentifier]] =
    PrintFutureMetric(s"filtering page modifier on ${pageReader.currentUrl}") {

      implicit val contextElement: Option[Element] = None

      def filter(pageIdentifier: WebpageIdentifier): Future[(Int, WebpageIdentifier)] = {

        import pageIdentifier.pageUniqueness._

        def urlRegexMatched() = Future(pageReader.currentUrl.matches(urlRegex))

        def anyUniqueTagNotFound() = Future {
          uniqueTags.exists { uniqueTag =>

            def tagTextExists(tagFound: WebElement): Boolean = {
              val uniqueTagText = uniqueTag.text.getOrElse("")
              if (uniqueTagText.length == 0) true
              else tagFound.getText != null && tagFound.getText.contains(uniqueTagText)
            }

            val tagFoundOpt = pageReader.findElement(FindElementBy.CSS_SELECTOR, uniqueTag.selector)(uniqueTag.contextElement)
            tagFoundOpt.isEmpty || !tagTextExists(tagFoundOpt.get)

          }
        }

        def uniqueStringNotExists() = Future {
          uniqueStrings.exists { uniqueString =>
            val stringContextOpt = pageReader.findElement(FindElementBy.TAG_NAME, "body")(uniqueString.contextElement)
            stringContextOpt.isEmpty ||
              stringContextOpt.get.getText == null ||
              !stringContextOpt.get.getText.contains(uniqueString.string)
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
            throw MultipleMatchingIdentifiersException(s"Two page identifiers matched for ${pageReader.currentUrl}. $sorted")
          } else {
            Some(sorted.head._2)
          }

        }
    }

  def clearCacheHistory(): Unit = {
    logger.warn("Clearing cache!!")
    pageReader.get("chrome://settings/clearBrowserData")
    FindElementAction(ActionName.CLICK, FindElementBy.ID, "clearBrowsingDataConfirm").perform(actionPerformer)(None)
  }
}

object ScrapeJobExecutor {
  def apply()(implicit pageReader: PageReader, db: Database): ScrapeJobExecutor = new ScrapeJobExecutor()

  val UrlRegexMatchScore = 47
  val UniqueTagMatchScore = 13
  val UniqueStringMatchScore = 2
  val NoMatch = 0
}
