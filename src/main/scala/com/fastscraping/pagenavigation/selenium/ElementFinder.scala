package com.fastscraping.pagenavigation.selenium

import com.fastscraping.model.Element
import com.fastscraping.pagenavigation.selenium.ElementFinder.PageContext
import org.openqa.selenium.remote.RemoteWebDriver
import org.openqa.selenium.{By, NoSuchElementException, WebElement}

import scala.jdk.CollectionConverters._

trait ElementFinder {
  def driver: RemoteWebDriver

  def findElementById(id: String)(implicit contextElement: Option[Element]): Option[WebElement] = {
    driver findOne By.id(id)
  }

  def findElementsById(id: String)(implicit contextElement: Option[Element]): Seq[WebElement] = {
    driver findMany By.id(id)
  }

  def findElementByClassName(className: String)(implicit contextElement: Option[Element]): Option[WebElement] = {
    driver findOne By.className(className)
  }

  def findElementsByClassName(className: String)(implicit contextElement: Option[Element]): Seq[WebElement] = {
    driver findMany By.className(className)
  }

  def findElementByLinkText(linkText: String)(implicit contextElement: Option[Element]): Option[WebElement] = {
    driver findOne By.linkText(linkText)
  }

  def findElementsByLinkText(linkText: String)(implicit contextElement: Option[Element]): Seq[WebElement] = {
    driver findMany By.linkText(linkText)
  }

  def findElementByPartialLinkText(partialLinkText: String)(implicit contextElement: Option[Element]) = {
    driver findOne By.partialLinkText(partialLinkText)
  }

  def findElementsByPartialLinkText(partialLinkText: String)(implicit contextElement: Option[Element]) = {
    driver findMany By.partialLinkText(partialLinkText)
  }

  def findElementByName(name: String)(implicit contextElement: Option[Element]): Option[WebElement] = {
    driver findOne By.name(name)
  }

  def findElementsByName(name: String)(implicit contextElement: Option[Element]): Seq[WebElement] = {
    driver findMany By.name(name)
  }

  def findElementByCssSelector(cssSelector: String)(implicit contextElement: Option[Element]): Option[WebElement] = {
    driver findOne By.cssSelector(cssSelector)
  }

  def findElementsByCssSelector(cssSelector: String)(implicit contextElement: Option[Element]): Seq[WebElement] = {
    driver findMany By.cssSelector(cssSelector)
  }

  def findElementByTagName(tagName: String)(implicit contextElement: Option[Element]): Option[WebElement] = {
    driver findOne By.tagName(tagName)
  }

  def findElementsByTagName(tagName: String)(implicit contextElement: Option[Element]): Seq[WebElement] = {
    driver findMany By.tagName(tagName)
  }

  def findElementByXPath(xPath: String)(implicit contextElement: Option[Element]): Option[WebElement] = {
    driver findOne By.xpath(xPath)
  }

  def findElementsByXPath(xPath: String)(implicit contextElement: Option[Element]): Seq[WebElement] = {
    driver findMany By.xpath(xPath)
  }

  def findElementByText(partialText: String)(implicit contextElement: Option[Element]): Option[WebElement] = {
    findElementByXPath(s"//*[text()='$partialText']")
  }

  def findElementsByText(partialText: String)(implicit contextElement: Option[Element]): Seq[WebElement] = {
    findElementsByXPath(s"//*[text()='$partialText']")
  }
}

object ElementFinder {

  object FindElementBy extends Enumeration {
    type FindElementBy = Value
    val BY_X_PATH, BY_TAG_NAME, BY_CSS_SELECTOR, BY_NAME, BY_PARTIAL_LINK_TEXT, BY_LINK_TEXT,
    BY_CLASS_NAME, BY_ID, BY_TEXT = Value
  }

  implicit class PageContext(webDriver: RemoteWebDriver) {

    private def Optional(f: => WebElement): Option[WebElement] = {
      try {
        Some(f)
      } catch {
        case _: NoSuchElementException => None
      }
    }

    def findOne(by: By)(implicit contextElement: Option[Element]): Option[WebElement] = {
      val theOne = Optional {
        if (contextElement.isDefined) {
          println(s"Context element is defined: $contextElement")
          val context = contextElement.get.find(webDriver)
          if (context.isDefined) {
            println(s"Context is defined: $context")
            val element = context.get.findElement(by)
            element.getLocation
            println(s"Got element with context: $element")
            println(s"Element in context. Tag name: ${element.getTagName} Displayed: ${element.isDisplayed} Is selected: ${element.isSelected}")

            element
          } else {
            webDriver.findElement(by)
          }
        } else {
          println(s"Context element is NOT defined: $contextElement")
          webDriver.findElement(by)
        }
      }

      println(s"Found the element $theOne")
      theOne
    }

    def findMany(by: By)(implicit contextElement: Option[Element]): Seq[WebElement] = {
      println(s"Finding elements by $by")

      val found = if (contextElement.isDefined) {
        println("Context element is defined")
        val context = contextElement.get.find(webDriver)
        if (context.isDefined) {
          context.get.findElements(by)
        } else {
          webDriver.findElements(by)
        }
      } else {
        println("Context element is not defined")
        webDriver.findElements(by)
      }

      found.asScala.toSeq
    }
  }

}
