package com.fastscraping.pageaction

import com.fastscraping.reader.PageReader
import com.fastscraping.utils.ElementNotFoundException
import org.openqa.selenium.WebElement
import org.openqa.selenium.interactions.Actions

import scala.concurrent.duration.Duration

class ActionPerformer(val pageReader: PageReader) {

  private val actions = new Actions(pageReader.driver)

  case class WithElement(selector: String) {
    def map(f: WebElement => Actions): Boolean = {
      val element = pageReader.findElementByCssSelector(selector)
        .getOrElse(throw ElementNotFoundException(s"$selector not found on ${pageReader.getCurrentUrl}"))

      f(element).perform()

      true
    }

    def flatMap[A](f: WebElement => A): A = {
      val element = pageReader.findElementByCssSelector(selector)
        .getOrElse(throw ElementNotFoundException(s"$selector not found on ${pageReader.getCurrentUrl}"))

      f(element)
    }
  }

  object WithoutElement {
    def apply(f: => Actions): Boolean = {
      f.perform()
      true
    }
  }

  def doubleClick(selector: String): Boolean = {
    WithElement(selector).map(element => actions.doubleClick(element))
  }

  def doubleClick: Boolean = WithoutElement(actions.doubleClick())

  def doubleClick(element: WebElement) = actions.doubleClick(element)

  def rightClick(selector: String): Boolean = {
    WithElement(selector).map(element => actions.contextClick(element))
  }

  def rightClick: Boolean = WithoutElement(actions.contextClick())

  def rightClick(element: WebElement) = {
    println(s"Right clicking on ${element}")
    actions.contextClick(element)
  }

  def contextClick(selector: String): Boolean = rightClick(selector)

  def contextClick: Boolean = WithoutElement(actions.contextClick())

  def contextClick(element: WebElement) = rightClick(element)

  def keyDown(selector: String, key: CharSequence): Boolean = {
    WithElement(selector).map(element => actions.keyDown(element, key))
  }

  def keyDown(key: CharSequence): Boolean = WithoutElement(actions.keyDown(key))

  def keyDown(element: WebElement, charSequence: CharSequence) = actions.keyDown(element, charSequence)

  def keyUp(selector: String, key: CharSequence): Boolean = {
    WithElement(selector).map(element => actions.keyUp(element, key))
  }

  def keyUp(key: CharSequence): Boolean = WithoutElement(actions.keyUp(key))

  def keyUp(element: WebElement, charSequence: CharSequence) = actions.keyUp(element, charSequence)

  def sendKeys(selector: String, key: CharSequence): Boolean = {
    WithElement(selector).map(element => actions.sendKeys(element, key))
  }

  /**
   * Sends keys to the active element. This differs from calling
   * WebElement#sendKeys(CharSequence...) on the active element in two ways:
   * <ul>
   * <li>The modifier keys included in this call are not released.</li>
   * <li>There is no attempt to re-focus the element - so sendKeys(Keys.TAB) for switching
   * elements should work. </li>
   * </ul>
   */
  def sendKeys(key: CharSequence): Boolean = WithoutElement(actions.sendKeys(key))

  def sendKeys(element: WebElement, charSequence: CharSequence) = actions.sendKeys(element, charSequence)

  /**
   * Clicks (without releasing) in the middle of the given element. This is equivalent to:
   * <i>Actions.moveToElement(onElement).clickAndHold()</i>
   */
  def clickAndHold(selector: String): Boolean = WithElement(selector).map(element => actions.clickAndHold(element))

  /**
   * Clicks (without releasing) at the current mouse location.
   */
  def clickAndHold: Boolean = WithoutElement(actions.clickAndHold())

  def clickAndHold(element: WebElement) = actions.clickAndHold(element)

  /**
   * Releases the depressed left mouse button, in the middle of the given element.
   * This is equivalent to:
   * <i>Actions.moveToElement(onElement).release()</i>
   *
   * Invoking this action without invoking clickAndHold() first will result in
   * undefined behaviour.
   */
  def release(selector: String): Boolean = WithElement(selector).map(element => actions.release(element))

  /**
   * Releases the depressed left mouse button at the current mouse location.
   */
  def release: Boolean = WithoutElement(actions.release())

  def release(element: WebElement) = actions.release(element)

  def click(selector: String): Boolean = {
    WithElement(selector).map(element => actions.click(element))
  }

  def click: Boolean = WithoutElement(actions.click())

  def click(element: WebElement) = actions.click(element)

  def mouseOverElement(selector: String): Boolean = moveToElement(selector)

  def mouseOverElement(element: WebElement) = actions.moveToElement(element)

  /**
   * Moves the mouse to the middle of the element. The element is scrolled into view and its
   * location is calculated using getBoundingClientRect.
   */
  def moveToElement(selector: String): Boolean = WithElement(selector).map(element => actions.moveToElement(element))

  /**
   * Moves the mouse to an offset from the top-left corner of the element.
   * The element is scrolled into view and its location is calculated using getBoundingClientRect.
   */
  def moveToElement(selector: String, xOffset: Int, yOffset: Int): Boolean = {
    WithElement(selector).map(element => actions.moveToElement(element, xOffset, yOffset))
  }

  def moveToElement(element: WebElement) = actions.moveToElement(element)

  /**
   * Moves the mouse from its current position (or 0,0) by the given offset. If the coordinates
   * provided are outside the viewport (the mouse will end up outside the browser window) then
   * the viewport is scrolled to match.                                       boundaries.
   */
  def moveByOffset(xOffset: Int, yOffset: Int): Actions = actions.moveByOffset(xOffset, yOffset)

  /**
   * A convenience method that performs click-and-hold at the location of the source element,
   * moves to the location of the target element, then releases the mouse.
   */
  def dragAndDrop(fromSelector: String, toSelector: String): Boolean = WithElement(fromSelector).flatMap { from =>
    WithElement(toSelector).map { to =>
      actions.dragAndDrop(from, to)
    }
  }

  def dragAndDrop(elementFrom: WebElement, elementTo: WebElement) = actions.dragAndDrop(elementFrom, elementTo)

  /**
   * A convenience method that performs click-and-hold at the location of the source element,
   * moves by a given offset, then releases the mouse.
   */
  def dragAndDropBy(selector: String, xOffset: Int, yOffset: Int): Boolean = WithElement(selector).map { element =>
    actions.dragAndDropBy(element, xOffset, yOffset)
  }

  def dragAndDropBy(element: WebElement, xOffset: Int, yOffset: Int) = actions.dragAndDropBy(element, xOffset, yOffset)

  def pause(millis: Long): Boolean = WithoutElement(actions.pause(millis))

  def pause(duration: Duration): Boolean = WithoutElement(actions.pause(java.time.Duration.ofMillis(duration.toMillis)))

}
