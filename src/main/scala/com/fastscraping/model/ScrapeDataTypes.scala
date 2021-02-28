package com.fastscraping.model

import com.fastscraping.utils.EnumSprayJsonFormat

object ScrapeDataTypes extends Enumeration {
  type ScrapeDataType = Value
  val TEXT, IMAGE = Value
  implicit val sprayJsonFormat = EnumSprayJsonFormat(ScrapeDataTypes)
}
