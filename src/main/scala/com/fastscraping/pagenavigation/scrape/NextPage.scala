package com.fastscraping.pagenavigation.scrape

import com.fastscraping.data.Database
import com.fastscraping.model.Element
import com.fastscraping.pagenavigation.action.{Action, ActionPerformer}
import com.fastscraping.pagenavigation.selenium.PageReader
import com.fastscraping.utils.Miscellaneous
import com.fastscraping.utils.Miscellaneous.{IS_CRAWLED, _LINK_TO_CRAWL}
import spray.json.DefaultJsonProtocol._
import spray.json.{JsValue, RootJsonFormat}

case class NextPage(withAction: Action,
                    doScrollDown: Option[Boolean] = None,
                    scrollRetries: Option[Int] = None) extends Scraping {

  override def name: String = s"NEXT_PAGE_WITH_${withAction.name}"

  override def collectionName(jobId: Option[String]): String = Miscellaneous.CrawlLinkCollection(jobId)

  override def scrape(jobId: Option[String])(implicit pageReader: PageReader, database: Database,
                                             contextElement: Option[Element]): Seq[PageData] =
    Miscellaneous.PrintMetric("Navigating to next page") {
      val actionPerformer = ActionPerformer(pageReader)

      withAction.perform(actionPerformer)
      val linkAfterNavigation = actionPerformer.pageReader.currentUrl
      val document: Map[String, AnyRef] = Map(
        _LINK_TO_CRAWL -> linkAfterNavigation.asInstanceOf[AnyRef],
        "_id" -> linkAfterNavigation.asInstanceOf[AnyRef],
        IS_CRAWLED -> false.asInstanceOf[AnyRef]
      )

      Seq(PageData(collectionName(jobId), document))
    }

  override def toJson: JsValue = NextPage.sprayJsonFmt.write(this)
}

object NextPage {
  implicit val sprayJsonFmt: RootJsonFormat[NextPage] = jsonFormat3(NextPage.apply)
}
