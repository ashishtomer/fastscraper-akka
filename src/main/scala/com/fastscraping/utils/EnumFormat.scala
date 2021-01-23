package com.fastscraping.utils

import play.api.libs.json.{Format, JsString, JsSuccess, JsValue, Reads, Writes}

object EnumFormat {
  def apply[E <: Enumeration](enum: E): Format[E#Value] = {
    val reads:Reads[E#Value] = (json: JsValue) => {
      val jsonString = json.toString()
      if(jsonString.startsWith("\"") && jsonString.endsWith("\"")) {
        JsSuccess(enum.withName(jsonString.substring(1, json.toString().length - 1)))
      } else {
        JsSuccess(enum.withName(jsonString))
      }
    }

    val writes: Writes[E#Value] = (obj: E#Value) => JsString(obj.toString.toUpperCase)

    Format (reads, writes)
  }
}
