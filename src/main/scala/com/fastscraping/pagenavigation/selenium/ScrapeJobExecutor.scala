package com.fastscraping.pagenavigation.selenium

import com.fastscraping.data.{Database, FsMongoDB}
import com.fastscraping.model.{Element, PageWork, WebpageIdentifier}
import com.fastscraping.pagenavigation.ActionPerformer
import com.fastscraping.pagenavigation.action.{Actions, TimeActions, WithPause}
import com.fastscraping.pagenavigation.scrape.Scraping
import com.fastscraping.utils.{IncorrectScrapeJob, MultipleMatchingIdentifiersException}

class ScrapeJobExecutor(implicit pageReader: PageReader, db: Database) {

  private val actionPerformer = ActionPerformer(pageReader)

  def execute(link: String, webpageIdentifiers: Seq[WebpageIdentifier]) = {

    pageReader.get(link)

    filterPageModifier(webpageIdentifiers) match {
      case Some(webpageIdentifier) => performOperations(webpageIdentifier)
      case None => println(s"No webpage identifier matched with ${pageReader.getCurrentUrl}")
    }

    db.markLinkAsScraped(link)
  }

  private def performOperations(webpageIdentifier: WebpageIdentifier) = {
    webpageIdentifier.pageWorks.map { pageWork =>
      implicit val ce = pageWork.contextElement
      WithPause(actionPerformer) {
        pageWork match {
          case PageWork(actions: Actions, contextElement) => actions.perform(actionPerformer)
          case PageWork(scraping: Scraping, contextElement) => scraping.scrape
        }
      }
    }
  }

  private def filterPageModifier[M](webpageIdentifiers: Seq[WebpageIdentifier]) = {
    implicit val contextElement: Option[Element] = None

    val identifierWithScore = webpageIdentifiers map { pageIdentifier =>

      val urlRegexMatched = pageReader.getCurrentUrl matches pageIdentifier.urlRegex

      val uniqueTagFound = pageReader.findElementByCssSelector(pageIdentifier.uniqueTag.selector) match {
        case Some(element) => pageIdentifier.uniqueTag.text.map(_ == element.getText)
        case None => Some(false)
      }

      val uniqueStringExists = pageIdentifier.uniqueStringOnPage.flatMap { uniqueString =>
        pageReader.findElementByTagName("body").map { body =>
          body.getText != null && body.getText.contains(uniqueString)
        }
      }

      val regexScore = if (urlRegexMatched) 47 else 0
      val tagScore = if (uniqueTagFound.isDefined && !uniqueTagFound.get) 0 else 13
      val stringScore = if (uniqueStringExists.isDefined && uniqueStringExists.get) 2 else 0

      val matchScore = regexScore + tagScore + stringScore

      (matchScore, pageIdentifier)
    }

    val sorted = identifierWithScore.sortWith((first, seconds) => first._1 > seconds._1)

    if (sorted.head._1 < ScrapeJobExecutor.UrlRegexMatchScore) {
      None
    } else if (sorted.size > 1 && sorted.head._1 == sorted.tail.head._1) {
      throw MultipleMatchingIdentifiersException("Two page identifiers matched for same page. ScrapeJob incorrect.")
    } else {
      Some(sorted.head._2)
    }
  }
}

object ScrapeJobExecutor {
  def apply()(implicit pageReader: PageReader, db: Database): ScrapeJobExecutor = new ScrapeJobExecutor()

  val UrlRegexMatchScore = 47
  val UniqueTagMatchScore = 13
  val UniqueStringMatchScore = 2
}
