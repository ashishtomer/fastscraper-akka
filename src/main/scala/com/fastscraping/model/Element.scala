package com.fastscraping.model

import com.fastscraping.pagenavigation.selenium.ElementFinder.FindElementBy
import com.fastscraping.pagenavigation.selenium.ElementFinder.FindElementBy.{BY_CLASS_NAME, BY_CSS_SELECTOR, BY_ID, BY_LINK_TEXT, BY_NAME, BY_PARTIAL_LINK_TEXT, BY_TAG_NAME, BY_TEXT, BY_X_PATH}
import org.openqa.selenium.remote.RemoteWebDriver
import play.api.libs.json.Json

import scala.util.{Failure, Success, Try}

case class Element(findBy: String, value: String) {
  def find(driver: RemoteWebDriver) = Try {
    FindElementBy.withName(findBy) match {
      case BY_X_PATH => driver.findElementByXPath(value)
      case BY_TAG_NAME => driver.findElementByTagName(value)
      case BY_CSS_SELECTOR => driver.findElementByCssSelector(value)
      case BY_NAME => driver.findElementByName(value)
      case BY_PARTIAL_LINK_TEXT => driver.findElementByPartialLinkText(value)
      case BY_LINK_TEXT => driver.findElementByLinkText(value)
      case BY_CLASS_NAME => driver.findElementByClassName(value)
      case BY_TEXT => driver.findElementByXPath(s"//*[text()='${value}']")
      case BY_ID => driver.findElementById(value)
    }
  } match {
    case Failure(_) => None
    case Success(value) => Some(value)
  }
}

object Element {
  implicit lazy val fmt = Json.format[Element]
}
