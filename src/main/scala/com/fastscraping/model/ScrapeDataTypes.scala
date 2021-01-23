package com.fastscraping.model

import com.fastscraping.utils.EnumFormat

object ScrapeDataTypes extends Enumeration {
  type ScrapeDataType = Value
  val TEXT, IMAGE = Value

  implicit lazy val fmt = EnumFormat(ScrapeDataTypes)
}
