package com.fastscraping.utils

object Miscellaneous {
  val CRAWL_LINK_COLLECTION = "crawl_links"
  val CRAWL_LINK_INDEX = "_link_to_crawl"
  val IS_CRAWLED = "is_crawled"

  val CrawlLinkCollection: Option[String] => String = jobIdOpt => {
    s"${CRAWL_LINK_COLLECTION}_${jobIdOpt.getOrElse("").replaceAll("[^\\w\\d_]", "_")}"
  }

  val CollectionByJobId: (Option[String], String) => String = (jobId, index) => {
    s"${index.replaceAll("[^\\w\\d_]", "_")}_${jobId.getOrElse("").replace("-", "_")}"
  }
}
