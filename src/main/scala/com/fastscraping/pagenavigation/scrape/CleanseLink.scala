package com.fastscraping.pagenavigation.scrape

import com.fastscraping.pagenavigation.scrape.CleanseLinkMethod.CleanseLinkMethod
import com.fastscraping.utils.EnumFormat
import play.api.libs.json.{Format, Json}


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
  implicit val fmt = Json.format[CleanseLink]
}

object CleanseLinkMethod extends Enumeration {
  type CleanseLinkMethod = Value
  val SUBSTRING_TILL = Value

  implicit val fmt: Format[CleanseLinkMethod.Value] = EnumFormat(CleanseLinkMethod)
}
