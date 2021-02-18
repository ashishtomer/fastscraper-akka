package com.fastscraping.pagenavigation.selenium

import com.fastscraping.data.Database
import com.fastscraping.model.{Element, PageWork, WebpageIdentifier}
import com.fastscraping.pagenavigation.ActionPerformer
import com.fastscraping.pagenavigation.action.{Actions, WithPause}
import com.fastscraping.pagenavigation.scrape.Scraping
import com.fastscraping.utils.Miscellaneous._
import com.fastscraping.utils.MultipleMatchingIdentifiersException
import org.openqa.selenium.WebElement

import scala.collection.mutable.ListBuffer
import scala.concurrent.{ExecutionContext, Future}

class ScrapeJobExecutor(implicit pageReader: PageReader, db: Database) {

  private val actionPerformer = ActionPerformer(pageReader)

  def execute(link: String, webpageIdentifiers: ListBuffer[WebpageIdentifier], jobId: Option[String])(implicit ec: ExecutionContext) = {

    pageReader.get(link)

    filterPageModifier(webpageIdentifiers) map {
      case Some(webpageIdentifier) => PrintMetric("performing operations") {performOperations(jobId, webpageIdentifier)}
      case None => println(s"No webpage identifier matched with ${pageReader.getCurrentUrl}")
    } map (_ => db.markLinkAsScraped(jobId, link))
  }

  private def performOperations(jobId: Option[String], webpageIdentifier: WebpageIdentifier): Seq[Any] = {
    webpageIdentifier.pageWorks.map { pageWork =>
      implicit val ce = pageWork.contextElement
      WithPause(actionPerformer) {
        pageWork match {
          case PageWork(actions: Actions, _) => actions.perform(actionPerformer)
          case PageWork(scraping: Scraping, _) => scraping.scrape(jobId)
        }
      }
    }
  }

  private def filterPageModifier[M](webpageIdentifiers: ListBuffer[WebpageIdentifier])(
    implicit ec: ExecutionContext): Future[Option[WebpageIdentifier]] = PrintMetric("filtering page modifier") {

    implicit val contextElement: Option[Element] = None

    def filter(pageIdentifier: WebpageIdentifier): Future[(Int, WebpageIdentifier)] = PrintMetric("running filter") {
      {
        import pageIdentifier.pageUniqueness._

        def urlRegexMatched() = Future(PrintMetric("matching url") {pageReader.getCurrentUrl matches urlRegex})

        def anyUniqueTagNotFound() = Future {
          PrintMetric("finding unique tag") {
            uniqueTags.exists { uniqueTag =>
              println(s"filter(): $uniqueTag [pageIdentifier= $pageIdentifier]")

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
              println(s"filter(): $uniqueString [pageIdentifier=$pageIdentifier]")
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
              println(s"regexMatched=$regexMatched uniqueTagMatched=$uniqueTagMatched stringNotExists=$stringNotExists")
              val regexScore = if (regexMatched) 47 else 0
              val tagScore = if (uniqueTagMatched) 0 else 13
              val stringScore = if (stringNotExists) 0 else 2

              println(s"regexScore=$regexScore tagScore=$tagScore stringScore=$stringScore identifier=$pageIdentifier")

              (regexScore + tagScore + stringScore, pageIdentifier)
          }
      }
    }

    val firstElementsMatch = filter(webpageIdentifiers.head)

    firstElementsMatch.map { firstMatch =>
      if (firstMatch._1 == 62) {
        println("First identifier's score is maximum. Won't filter against other identifiers.")
        return Future.successful(Some(firstMatch._2))
      }
    }

    RunFuturesInParallel(firstElementsMatch +: webpageIdentifiers.tail.map(x => filter(x)).toSeq)
      .map { identifierWithScore =>
        val sorted = identifierWithScore.sortWith((first, seconds) => first._1 > seconds._1)

        println(s"Before sorting: $webpageIdentifiers")
        webpageIdentifiers.clear()
        val sortedIdentifiers = webpageIdentifiers.appendAll(sorted.map(_._2))
        println(s"After sorting: $sortedIdentifiers")

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
