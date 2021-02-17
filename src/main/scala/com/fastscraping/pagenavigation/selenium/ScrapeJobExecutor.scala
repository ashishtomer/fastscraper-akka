package com.fastscraping.pagenavigation.selenium

import com.fastscraping.data.Database
import com.fastscraping.model.{Element, PageWork, WebpageIdentifier}
import com.fastscraping.pagenavigation.ActionPerformer
import com.fastscraping.pagenavigation.action.{Actions, WithPause}
import com.fastscraping.pagenavigation.scrape.Scraping
import com.fastscraping.utils.MultipleMatchingIdentifiersException

class ScrapeJobExecutor(implicit pageReader: PageReader, db: Database) {

  private val actionPerformer = ActionPerformer(pageReader)

  def execute(link: String, webpageIdentifiers: Seq[WebpageIdentifier], jobId: Option[String]) = {

    println(s"Opening link: $link")

    pageReader.get(link)

    filterPageModifier(webpageIdentifiers) match {
      case Some(webpageIdentifier) => performOperations(jobId, webpageIdentifier)
      case None => println(s"No webpage identifier matched with ${pageReader.getCurrentUrl}")
    }

    db.markLinkAsScraped(jobId, link)
  }

  private def performOperations(jobId: Option[String], webpageIdentifier: WebpageIdentifier) = {
    webpageIdentifier.pageWorks.map { pageWork =>
      implicit val ce = pageWork.contextElement
      WithPause(actionPerformer) {
        pageWork match {
          case PageWork(actions: Actions, contextElement) => actions.perform(actionPerformer)
          case PageWork(scraping: Scraping, contextElement) => scraping.scrape(jobId)
        }
      }
    }
  }

  private def filterPageModifier[M](webpageIdentifiers: Seq[WebpageIdentifier]) = {
    implicit val contextElement: Option[Element] = None

    val identifierWithScore = webpageIdentifiers map { pageIdentifier =>
      import pageIdentifier.pageUniqueness._

      val urlRegexMatched = pageReader.getCurrentUrl matches urlRegex

      val uniqueTagFound = pageReader.findElementsByTagName(uniqueTag.selector)(uniqueTag.contextElement)
        .exists(element => element.getText.contains(uniqueString.string.trim))

      val uniqueStringExists = pageReader.findElementByTagName("body")(uniqueString.contextElement)
        .map(context => context.getText != null && context.getText.contains(uniqueString.string))

      val regexScore = if (urlRegexMatched) 47 else 0
      val tagScore = if (uniqueTagFound) 13 else 0
      val stringScore = if (uniqueStringExists.isDefined && uniqueStringExists.get) 2 else 0

      println(s"${pageReader.getCurrentUrl} => [regexScore=$regexScore][tagScore=$tagScore][stringScore=$stringScore] $pageIdentifier")

      val matchScore = regexScore + tagScore + stringScore

      (matchScore, pageIdentifier)
    }

    val sorted = identifierWithScore.sortWith((first, seconds) => first._1 > seconds._1)

    if (sorted.head._1 < ScrapeJobExecutor.UrlRegexMatchScore) {
      None
    } else if (sorted.size > 1 && sorted.head._1 == sorted.tail.head._1) {
      throw MultipleMatchingIdentifiersException(s"Two page identifiers matched for ${pageReader.getCurrentUrl}. $sorted")
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
