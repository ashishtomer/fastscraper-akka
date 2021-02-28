package com.fastscraping.model

import com.fastscraping.pagenavigation.selenium.ElementFinder.FindElementBy.{CLASS_NAME, CSS_SELECTOR, FindElementBy, ID, LINK_TEXT, NAME, PARTIAL_LINK_TEXT, TAG_NAME, TEXT, X_PATH}
import org.openqa.selenium.remote.RemoteWebDriver
import spray.json.DefaultJsonProtocol._
import spray.json.JsonFormat

import scala.util.{Failure, Success, Try}

case class Element(findBy: FindElementBy, value: String) {
  def find(driver: RemoteWebDriver) = Try {
    findBy match {
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
  implicit val sprayJsonFmt: JsonFormat[Element] = jsonFormat2(Element.apply)
}
