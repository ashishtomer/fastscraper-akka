package com.fastscraping.model

import com.fastscraping.pagenavigation.selenium.ElementFinder.FindElementBy
import com.fastscraping.pagenavigation.selenium.ElementFinder.FindElementBy.{CLASS_NAME, CSS_SELECTOR, ID, LINK_TEXT, NAME, PARTIAL_LINK_TEXT, TAG_NAME, TEXT, X_PATH}
import org.openqa.selenium.remote.RemoteWebDriver
import play.api.libs.json.Json

import scala.util.{Failure, Success, Try}

case class Element(findBy: String, value: String) {
  def find(driver: RemoteWebDriver) = Try {
    FindElementBy.withName(findBy) match {
      case X_PATH => driver.findElementByXPath(value)
      case TAG_NAME => driver.findElementByTagName(value)
      case CSS_SELECTOR => driver.findElementByCssSelector(value)
      case NAME => driver.findElementByName(value)
      case PARTIAL_LINK_TEXT => driver.findElementByPartialLinkText(value)
      case LINK_TEXT => driver.findElementByLinkText(value)
      case CLASS_NAME => driver.findElementByClassName(value)
      case TEXT => driver.findElementByXPath(s"//*[text()='${value}']")
      case ID => driver.findElementById(value)
    }
  } match {
    case Failure(_) => None
    case Success(value) => Some(value)
  }
}

object Element {
  implicit lazy val fmt = Json.format[Element]
}
