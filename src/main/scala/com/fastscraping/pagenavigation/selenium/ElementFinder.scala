package com.fastscraping.pagenavigation.selenium

import org.openqa.selenium.remote.RemoteWebDriver
import org.openqa.selenium.{By, NoSuchElementException, WebElement}

import scala.jdk.CollectionConverters._

trait ElementFinder {
  def driver: RemoteWebDriver

  def findElementById(id: String): Option[WebElement] = Optional(driver.findElement(By.id(id)))

  def findElementsById(id: String): Seq[WebElement] = driver.findElements(By.id(id)).asScala.toSeq

  def findElementByClassName(className: String): Option[WebElement] = {
    Optional(driver.findElement(By.className(className)))
  }

  def findElementsByClassName(className: String): Seq[WebElement] = {
    driver.findElements(By.className(className)).asScala.toSeq
  }

  def findElementByLinkText(linkText: String): Option[WebElement] = Optional(driver.findElement(By.linkText(linkText)))


  def findElementsByLinkText(linkText: String): Seq[WebElement] = {
    driver.findElements(By.linkText(linkText)).asScala.toSeq
  }

  def findElementByPartialLinkText(partialLinkText: String): Option[WebElement] = {
    Optional(driver.findElement(By.partialLinkText(partialLinkText)))
  }

  def findElementsByPartialLinkText(partialLinkText: String): Seq[WebElement] = {
    driver.findElements(By.partialLinkText(partialLinkText)).asScala.toSeq
  }

  def findElementByName(name: String): Option[WebElement] = Optional(driver.findElement(By.name(name)))

  def findElementsByName(name: String): Seq[WebElement] = driver.findElements(By.name(name)).asScala.toSeq

  def findElementByCssSelector(cssSelector: String): Option[WebElement] = {
    Optional(driver.findElement(By.cssSelector(cssSelector)))
  }

  def findElementsByCssSelector(cssSelector: String): Seq[WebElement] = {
    driver.findElements(By.cssSelector(cssSelector)).asScala.toSeq
  }

  def findElementByTagName(tagName: String): Option[WebElement] = Optional(driver.findElement(By.tagName(tagName)))

  def findElementsByTagName(tagName: String): Seq[WebElement] = driver.findElements(By.tagName(tagName)).asScala.toSeq

  def findElementByXPath(xPath: String): Option[WebElement] = Optional(driver.findElement(By.tagName(xPath)))

  def findElementsByXPath(xPath: String): Seq[WebElement] = driver.findElements(By.tagName(xPath)).asScala.toSeq

  def Optional(f: => WebElement) = {
    try {
      Some(f)
    } catch {
      case  ex: NoSuchElementException => None
    }
  }
}

object ElementFinder {
  object FindElementBy extends Enumeration {
    type FindElementBy = Value
    val BY_X_PATH, BY_TAG_NAME, BY_CSS_SELECTOR, BY_NAME, BY_PARTIAL_LINK_TEXT, BY_LINK_TEXT,
    BY_CLASS_NAME, BY_ID = Value
  }
}
