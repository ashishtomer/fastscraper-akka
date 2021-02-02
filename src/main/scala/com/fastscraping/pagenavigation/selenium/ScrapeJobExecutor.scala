package com.fastscraping.pagenavigation.selenium

import com.fastscraping.data.Database
import com.fastscraping.pagenavigation.action.{Actions, TimeActions, WithPause}
import com.fastscraping.model.WebpageIdentifier
import com.fastscraping.pagenavigation.{ActionPerformer, Scraper}
import com.fastscraping.pagenavigation.scrape.ScrapeData
import com.fastscraping.utils.{IncorrectScrapeJob, MultipleMatchingIdentifiersException}

class ScrapeJobExecutor(pageReader: PageReader) {

  val actionPerformer = new ActionPerformer(pageReader)
  val scraper = new Scraper(pageReader, new Database{})
  val timerAction = new TimeActions(100)

  def execute(webpageIdentifiers: Seq[WebpageIdentifier]) = {
    if (webpageIdentifiers.isEmpty) {
      throw IncorrectScrapeJob("No web page identifier found")
    }

    filterPageModifier(webpageIdentifiers) match {
      case Some(webpageIdentifier) => performOperations(webpageIdentifier)
      case None => ()
    }
  }

  private def performOperations(webpageIdentifier: WebpageIdentifier) = {
    webpageIdentifier.actionsAndScrapeData.map { workForPage =>
      WithPause(actionPerformer) {
        workForPage match {
          case actions: Actions => actions.perform(actionPerformer)
          case scrapeData: ScrapeData => scrapeData.extractData(scraper)
        }
      }
    }
  }

  private def filterPageModifier[M](webpageIdentifiers: Seq[WebpageIdentifier]) = {
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
  def apply(pageReader: PageReader): ScrapeJobExecutor = new ScrapeJobExecutor(pageReader)

  val UrlRegexMatchScore = 47
  val UniqueTagMatchScore = 13
  val UniqueStringMatchScore = 2
}
