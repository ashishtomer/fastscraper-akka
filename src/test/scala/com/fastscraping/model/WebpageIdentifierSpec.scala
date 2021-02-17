package com.fastscraping.model

import com.fastscraping.pagenavigation.ActionsAndScrape
import com.fastscraping.pagenavigation.action.SelectorActions
import com.fastscraping.pagenavigation.scrape.ScrapeWithSelector
import org.scalatest.WordSpecLike
import play.api.libs.json.Json

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

      val parsed = Json.parse(testJson.toString)
      val actionsAndScrapeData = parsed.as[ActionsAndScrape]

      assert(actionsAndScrapeData.isInstanceOf[SelectorActions])
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

      val parsed = Json.parse(testJson.toString)
      val actionsAndScrapeData = parsed.as[ActionsAndScrape]

      assert(actionsAndScrapeData.isInstanceOf[ScrapeWithSelector])
    }

    "write json from instance of Actions" in {
      val actions = SelectorActions("div.classb > p", Some("click")).asInstanceOf[ActionsAndScrape]
      val jsonText = Json.toJson(actions).toString()

      assert(jsonText.contains("action"))
    }

    "write json from instance of ScrapeData" in {
      val actions = ScrapeWithSelector("div.classb > p", DataToExtract("class b text", ScrapeDataTypes.TEXT), "text2").asInstanceOf[ActionsAndScrape]
      val jsonText = Json.toJson(actions).toString()

      assert(jsonText.contains("dataType"))
      assert(jsonText.contains("storageKey"))
    }

    "write json from ScrapingJob instance" in {
      val scrapingJob = WebpageIdentifier(
        PageUniqueness("http://welcome.com", UniqueTag("<h1>", Some("Welcome heading")), UniqueString("Welcome")),

        Seq(
          PageWork(ScrapeWithSelector("div.classb > p", DataToExtract("class b text", ScrapeDataTypes.TEXT), "text2")),
          PageWork(SelectorActions("div.classb > p > button", Some("click")))
        )
      )

      val jsonString = Json.prettyPrint(Json.toJson(scrapingJob))

      assert(jsonString.contains("urlRegex"))
      assert(jsonString.contains("uniqueStringOnPage"))
      assert(jsonString.contains("uniqueTag"))
      assert(jsonString.contains("actionsAndScrapeData"))
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

      val scrapingJob = Json.parse(jsonText).asOpt[WebpageIdentifier]
      assert(scrapingJob.isDefined)
    }
  }
}
