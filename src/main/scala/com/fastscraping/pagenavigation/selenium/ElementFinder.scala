package com.fastscraping.pagenavigation.selenium

import com.fastscraping.model.Element
import com.fastscraping.pagenavigation.action.ActionPerformer
import com.fastscraping.pagenavigation.selenium.ElementFinder.PageContext
import com.fastscraping.utils.{EnumSprayJsonFormat, FsLogging}
import org.openqa.selenium.remote.RemoteWebDriver
import org.openqa.selenium.{By, NoSuchElementException, WebElement}
import spray.json.JsonFormat

import scala.jdk.CollectionConverters._
import scala.util.control.NonFatal

trait ElementFinder extends FsLogging {
  pageReader: PageReader =>

  import com.fastscraping.pagenavigation.selenium.ElementFinder.FindElementBy._

  def driver: RemoteWebDriver

  def findElement(by: FindElementBy, value: String)(implicit ce: Option[Element]): Option[WebElement] = {
    by match {
      case ID => findElementById(value)
      case X_PATH => findElementByXPath(value)
      case TAG_NAME => findElementByTagName(value)
      case CSS_SELECTOR => findElementByCssSelector(value)
      case NAME => findElementByName(value)
      case PARTIAL_LINK_TEXT => findElementByPartialLinkText(value)
      case LINK_TEXT => findElementByLinkText(value)
      case CLASS_NAME => findElementByClassName(value)
    }
  }

  def findElements(by: FindElementBy, value: String)(implicit contextElement: Option[Element]): Seq[WebElement] = {
    by match {
      case ID => findElementsById(value)
      case X_PATH => findElementsByXPath(value)
      case TAG_NAME => findElementsByTagName(value)
      case CSS_SELECTOR => findElementsByCssSelector(value)
      case NAME => findElementsByName(value)
      case PARTIAL_LINK_TEXT => findElementsByPartialLinkText(value)
      case LINK_TEXT => findElementsByLinkText(value)
      case CLASS_NAME => findElementsByClassName(value)
    }
  }

  private def findElementById(id: String)(implicit contextElement: Option[Element]): Option[WebElement] = WithRetry {
    driver findOne By.id(id)
  }

  private def findElementsById(id: String)(implicit contextElement: Option[Element]): Seq[WebElement] = WithRetry {
    driver findMany By.id(id)
  }

  private def findElementByClassName(className: String)(implicit contextElement: Option[Element]): Option[WebElement] = WithRetry {
    driver findOne By.className(className)
  }

  private def findElementsByClassName(className: String)(implicit contextElement: Option[Element]): Seq[WebElement] = WithRetry {
    driver findMany By.className(className)
  }

  private def findElementByLinkText(linkText: String)(implicit contextElement: Option[Element]): Option[WebElement] = WithRetry {
    driver findOne By.linkText(linkText)
  }

  private def findElementsByLinkText(linkText: String)(implicit contextElement: Option[Element]): Seq[WebElement] = WithRetry {
    driver findMany By.linkText(linkText)
  }

  private def findElementByPartialLinkText(partialLinkText: String)(implicit contextElement: Option[Element]) = WithRetry {
    driver findOne By.partialLinkText(partialLinkText)
  }

  private def findElementsByPartialLinkText(partialLinkText: String)(implicit contextElement: Option[Element]) = WithRetry {
    driver findMany By.partialLinkText(partialLinkText)
  }

  private def findElementByName(name: String)(implicit contextElement: Option[Element]): Option[WebElement] = WithRetry {
    driver findOne By.name(name)
  }

  private def findElementsByName(name: String)(implicit contextElement: Option[Element]): Seq[WebElement] = WithRetry {
    driver findMany By.name(name)
  }

  private def findElementByCssSelector(cssSelector: String)(implicit contextElement: Option[Element]) = WithRetry {
    driver findOne By.cssSelector(cssSelector)
  }

  private def findElementsByCssSelector(cssSelector: String)(implicit contextElement: Option[Element]) = WithRetry {
    driver findMany By.cssSelector(cssSelector)
  }

  private def findElementByTagName(tagName: String)(implicit contextElement: Option[Element]) = WithRetry {
    driver findOne By.tagName(tagName)
  }

  private def findElementsByTagName(tagName: String)(implicit contextElement: Option[Element]): Seq[WebElement] = WithRetry {
    driver findMany By.tagName(tagName)
  }

  private def findElementByXPath(xPath: String)(implicit contextElement: Option[Element]): Option[WebElement] = WithRetry {
    driver findOne By.xpath(xPath)
  }

  private def findElementsByXPath(xPath: String)(implicit contextElement: Option[Element]): Seq[WebElement] = WithRetry {
    driver findMany By.xpath(xPath)
  }

  private def findElementByText(partialText: String)(implicit contextElement: Option[Element]): Option[WebElement] = WithRetry {
    findElementByXPath(s"//*[text()='$partialText']")
  }

  private def findElementsByText(partialText: String)(implicit contextElement: Option[Element]): Seq[WebElement] = WithRetry {
    findElementsByXPath(s"//*[text()='$partialText']")
  }

  private def WithRetry[T](f: => T): T = {
    var retryMillis = 100

    def tryExecution: T = try (f) catch {
      case NonFatal(ex) =>
        if (retryMillis < 2000) {
          logger.info(s"Retrying execution in $retryMillis milliseconds after error", ex)
          ActionPerformer(pageReader).pause(retryMillis)
          retryMillis = retryMillis * 2
          tryExecution
        } else {
          logger.error("Could not perform find operation", ex)
          throw ex
        }
    }

    tryExecution
  }
}

object ElementFinder extends FsLogging {

  object FindElementBy extends Enumeration {
    type FindElementBy = Value
    val X_PATH, TAG_NAME, CSS_SELECTOR, NAME, PARTIAL_LINK_TEXT, LINK_TEXT, CLASS_NAME, ID, TEXT = Value
    implicit val sprayJsonFmt: JsonFormat[ElementFinder.FindElementBy.Value] = EnumSprayJsonFormat(FindElementBy)
  }

  implicit class PageContext(webDriver: RemoteWebDriver) {

    private def Optional(f: => WebElement): Option[WebElement] = {
      try {
        Some(f)
      } catch {
        case e: NoSuchElementException =>
          logger.warn(s"The element not found on ${webDriver.getCurrentUrl}")
          None
      }
    }

    def findOne(by: By)(implicit contextElement: Option[Element]): Option[WebElement] = {
      Optional {
        if (contextElement.isDefined) {
          val context = contextElement.get.find(webDriver)
          if (context.isDefined) {
            context.get.findElement(by)
          } else {
            webDriver.findElement(by)
          }
        } else {
          webDriver.findElement(by)
        }
      }
    }

    def findMany(by: By)(implicit contextElement: Option[Element]): Seq[WebElement] = {
      val found = if (contextElement.isDefined) {
        val context = contextElement.get.find(webDriver)
        if (context.isDefined) {
          context.get.findElements(by)
        } else {
          webDriver.findElements(by)
        }
      } else {
        webDriver.findElements(by)
      }

      found.asScala.toSeq
    }
  }

}
