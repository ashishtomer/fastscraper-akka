package com.fastscraping.utils

import spray.json.{JsString, JsValue, RootJsonFormat}

object EnumSprayJsonFormat {
  def apply[E <: Enumeration](enum: E): RootJsonFormat[E#Value] = new RootJsonFormat[E#Value] {
    override def read(json: JsValue): E#Value = {
      val jsString = json.toString.trim
      if(jsString.startsWith("\"") && jsString.endsWith("\"")) {
        `enum`.withName(jsString.substring(1, jsString.length - 1))
      } else {
        enum.withName(jsString)
      }
    }
    override def write(obj: E#Value): JsValue = JsString(obj.toString)
  }
}
