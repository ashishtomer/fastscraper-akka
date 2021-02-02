package com.fastscraping.data

trait Database {
  def saveText(key: String, text: String): Unit = {
    println(s"The data is saved under key: $key. >> $text")
  }
}
