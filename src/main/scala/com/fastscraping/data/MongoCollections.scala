package com.fastscraping.data

object MongoCollections {
  case class CrawlLinkCollection(_link_to_crawl: String, is_crawled: Boolean)
}
