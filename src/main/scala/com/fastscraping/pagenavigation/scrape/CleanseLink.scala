package com.fastscraping.pagenavigation.scrape

import com.fastscraping.pagenavigation.scrape
import com.fastscraping.pagenavigation.scrape.CleanseLinkMethod.CleanseLinkMethod
import com.fastscraping.utils.EnumSprayJsonFormat
import spray.json.DefaultJsonProtocol._
import spray.json.RootJsonFormat


case class CleanseLink(cleanseLinkMethod: CleanseLinkMethod, value: String) {

  import CleanseLinkMethod._

  def cleanse(linkToCleanse: String): String = {
    cleanseLinkMethod match {
      case SUBSTRING_TILL =>
        val tillIndex = linkToCleanse.indexOf(value)
        if (tillIndex > 0) linkToCleanse.substring(0, tillIndex)
        else linkToCleanse
    }
  }
}

object CleanseLink {
  implicit val sprayJsonFmt = jsonFormat2(CleanseLink.apply)
}

object CleanseLinkMethod extends Enumeration {
  type CleanseLinkMethod = Value
  val SUBSTRING_TILL = Value

  implicit val sprayJsonFmt: RootJsonFormat[scrape.CleanseLinkMethod.Value] = EnumSprayJsonFormat(CleanseLinkMethod)
}
