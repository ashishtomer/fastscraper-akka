package com.fastscraping.pagenavigation.scrape

object ScrapeType extends Enumeration {
  type ScrapeType = Value
  val SCRAPE_TEXT_WITH_SELECTOR, SCRAPE_LINKS, SCRAPE_CRAWL_LINKS = Value
}
