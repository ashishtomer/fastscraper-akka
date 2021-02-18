package com.fastscraping.pagenavigation.selenium

import com.fastscraping.data.Database
import com.fastscraping.model.{Element, PageWork, UniqueString, UniqueTag, WebpageIdentifier}
import com.fastscraping.pagenavigation.ActionPerformer
import com.fastscraping.pagenavigation.action.{Actions, WithPause}
import com.fastscraping.pagenavigation.scrape.Scraping
import com.fastscraping.utils.MultipleMatchingIdentifiersException

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

class ScrapeJobExecutor(implicit pageReader: PageReader, db: Database) {

  private val actionPerformer = ActionPerformer(pageReader)

  def execute(link: String, webpageIdentifiers: ListBuffer[WebpageIdentifier], jobId: Option[String]) = {

    pageReader.get(link)

    filterPageModifier(webpageIdentifiers) match {
      case Some(webpageIdentifier) => performOperations(jobId, webpageIdentifier)
      case None => println(s"No webpage identifier matched with ${pageReader.getCurrentUrl}")
    }

    db.markLinkAsScraped(jobId, link)
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

  private def filterPageModifier[M](webpageIdentifiers: ListBuffer[WebpageIdentifier]): Option[WebpageIdentifier] = {
    implicit val contextElement: Option[Element] = None

    def filter(pageIdentifier: WebpageIdentifier): (Int, WebpageIdentifier) = {
      import pageIdentifier.pageUniqueness._

      val urlRegexMatched = pageReader.getCurrentUrl matches urlRegex

      val tagsForUniqueness = pageReader.findElementsByCssSelector(uniqueTag.selector)(uniqueTag.contextElement)
      val uniqueTagFound: Boolean = if (tagsForUniqueness.nonEmpty) {
        uniqueTag.text match {
          case None => true
          case Some(x) if x.trim.isEmpty => true
          case Some(tagsText) => tagsForUniqueness.exists(_.getText.contains(tagsText))
        }
      } else {
        false
      }

      val uniqueStringExists = pageReader.findElementByTagName("body")(uniqueString.contextElement)
        .map(context => context.getText != null && context.getText.contains(uniqueString.string))

      val regexScore = if (urlRegexMatched) 47 else 0
      val tagScore = if (uniqueTagFound) 13 else 0
      val stringScore = if (uniqueStringExists.isDefined && uniqueStringExists.get) 2 else 0

      println(s"regexScore=$regexScore tagScore=$tagScore stringScore=$stringScore identifier=$pageIdentifier")

      val matchScore = regexScore + tagScore + stringScore

      (matchScore, pageIdentifier)
    }

    val firstElementsMatch = filter(webpageIdentifiers.head)
    if(firstElementsMatch._1 == 62) {
      Some(firstElementsMatch._2)
    } else {

      val identifierWithScore: mutable.Seq[(Int, WebpageIdentifier)] = firstElementsMatch +: webpageIdentifiers.tail.map(x => filter(x))

      val sorted = identifierWithScore.sortWith((first, seconds) => first._1 > seconds._1)

      println(s"Before sorting: $webpageIdentifiers")
      webpageIdentifiers.clear()
      val sortedIdentifiers = webpageIdentifiers.appendAll(sorted.map(_._2))
      println(s"After sorting: $sortedIdentifiers")

      println(sorted)

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
