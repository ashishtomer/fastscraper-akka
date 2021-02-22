package com.fastscraping.utils

import scala.concurrent.{ExecutionContext, Future, Promise}
import scala.util.{Failure, Success}

object Miscellaneous extends FsLogging {
  val CRAWL_LINK_COLLECTION = "crawl_links"
  val _LINK_TO_CRAWL = "_link_to_crawl"
  val IS_CRAWLED = "is_crawled"

  val CrawlLinkCollection: Option[String] => String = jobIdOpt => {
    s"${CRAWL_LINK_COLLECTION}_${jobIdOpt.getOrElse("").replaceAll("[^\\w\\d_]", "_")}"
  }

  val CollectionByJobId: (Option[String], String) => String = (jobId, index) => {
    s"${index.replaceAll("[^\\w\\d_]", "_")}_${jobId.getOrElse("").replace("-", "_")}"
  }

  def RunFuturesInParallel[A](futures: Seq[Future[A]])(implicit ec: ExecutionContext) = {
    val promAndFuts = futures.foldLeft(Seq[(Future[A], Promise[A])]())((promises, fut) => (fut, Promise[A]()) +: promises)

    Future.sequence {
      promAndFuts.map {
        case (future, promise) =>
          future.onComplete {
            case Success(a: A) => promise.success(a)
            case Failure(e) => promise.failure(e)
          }

          promise.future
      }
    }
  }

  def PrintMetric[T](metricName: String)(f: => T): T = {
    val start = System.currentTimeMillis()
    val ret = f
    logger.info(s"Time taken in $metricName: ${System.currentTimeMillis() - start}")
    ret
  }
}
