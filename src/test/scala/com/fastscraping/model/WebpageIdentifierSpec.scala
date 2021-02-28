package com.fastscraping.model

import com.fastscraping.pagenavigation.ActionsAndScrape
import com.fastscraping.pagenavigation.action.SelectorAction
import com.fastscraping.pagenavigation.scrape.ScrapeData
import com.fastscraping.pagenavigation.selenium.ElementFinder.FindElementBy
import org.scalatest.WordSpecLike

class WebpageIdentifierSpec extends WordSpecLike {
  "ActionsAndScrapeData" should {
    "read json to Actions" in {
      val testJson =
        """
          |{
          | "selector": "div.classb > p",
          | "action": "click"
          |}
          |""".stripMargin


      assert(true)
    }

    "read json to ScrapeData" in {
      val testJson =
        """
          |{
          | "selector": "div.classb > p",
          | "dataToExtract": {
          |   "storageKey" : "class b text",
          |   "dataType" : "TEXT"
          | }
          |}
          |""".stripMargin

      assert(true)
    }

    "write json from instance of Actions" in {
      val actions = SelectorAction("div.classb > p", Some("click")).asInstanceOf[ActionsAndScrape]

      assert(true)
    }

    "write json from instance of ScrapeData" in {
      val actions = ScrapeData(FindElementBy.CSS_SELECTOR, "div.classb > p", DataToExtract("class b text", ScrapeDataTypes.TEXT), "text2").asInstanceOf[ActionsAndScrape]

      assert(true)
    }

    "write json from ScrapingJob instance" in {
      val scrapingJob = WebpageIdentifier(
        PageUniqueness("http://welcome.com", Seq(UniqueTag("<h1>", Some("Welcome heading"))), Seq(UniqueString("Welcome"))),

        Seq(
          PageWork(ScrapeData(FindElementBy.CSS_SELECTOR, "div.classb > p", DataToExtract("class b text", ScrapeDataTypes.TEXT), "text2")),
          PageWork(SelectorAction("div.classb > p > button", Some("click")))
        )
      )

      assert(true)
    }

    "read json to create ScrapingJob" in {
      val jsonText =
        """
          |{
          |  "urlRegex" : "http://welcome.com",
          |  "uniqueStringOnPage" : "Welcome",
          |  "uniqueTag" : {
          |    "selector" : "<h1>",
          |    "text" : "Welcome heading"
          |  },
          |  "actionsAndScrapeData" : [ {
          |    "selector" : "div.classb > p",
          |    "dataToExtract" : {
          |      "storageKey" : "class b text",
          |      "dataType" : "TEXT"
          |    }
          |  }, {
          |    "selector" : "div.classb > p > button",
          |    "action" : "click"
          |  } ]
          |}
          |""".stripMargin

      assert(true)
    }
  }
}
