package com.fastscraping.utils

abstract class FastscrapingException(msg: String) extends IllegalArgumentException(msg)

case class JsonParsingException(msg: String, json: Option[String])
  extends FastscrapingException(msg + json.map(js => "\n" + js).getOrElse(""))

case class JsonWriteException(msg: String) extends FastscrapingException(msg)

case class MultipleMatchingIdentifiersException(msg: String) extends FastscrapingException(msg)

case class ElementNotFoundException(msg: String) extends FastscrapingException(msg)

case class IncorrectScrapeJob(msg: String) extends FastscrapingException(msg)

case class ConfigurationNotFoundException(msg: String) extends FastscrapingException(msg)
