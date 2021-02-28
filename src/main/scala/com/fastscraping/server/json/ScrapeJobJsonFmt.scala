package com.fastscraping.server.json

import com.fastscraping.actor.message.ScrapeJob
import spray.json.{JsValue, RootJsonFormat}

object ScrapeJobJsonFmt {
  implicit object Format extends RootJsonFormat[ScrapeJob] {
    override def read(json: JsValue): ScrapeJob = {
      json.convertTo[ScrapeJob]
    }

    override def write(obj: ScrapeJob): JsValue = ???
  }

}
