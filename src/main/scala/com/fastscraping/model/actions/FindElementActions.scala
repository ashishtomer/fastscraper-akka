package com.fastscraping.model.actions

import com.fastscraping.model.actions.ActionNames._
import com.fastscraping.pageaction.ActionPerformer
import com.fastscraping.reader.ElementFinder.FindElementBy
import com.fastscraping.utils.ExHelpers.getElement
import org.openqa.selenium.WebElement
import play.api.libs.json.{Format, Json}

case class FindElementActions(action: String,
                              findElementBy: String,
                              value: String,
                              multiple: Boolean = false,
                              times: Int = 1,
                              pauseBeforeActionMillis: Long = 100L) extends Actions {

  import FindElementBy._

  override val name = s"${action}_$findElementBy"

  private def withElements[A](performer: ActionPerformer)(f: Seq[WebElement] => A): A = {
    val elems: Seq[WebElement] = FindElementBy.withName(findElementBy) match {
      case BY_X_PATH =>
        if (multiple) {
          performer.pageReader.findElementsByXPath(value)
        } else {
          Seq(getElement(performer.pageReader.findElementByXPath(value))(Some(s"$BY_X_PATH : $value not found")))
        }
      case BY_TAG_NAME =>
        if (multiple) {
          performer.pageReader.findElementsByTagName(value)
        } else {
          Seq(getElement(performer.pageReader.findElementByTagName(value))(Some(s"$BY_TAG_NAME : $value not found")))
        }
      case BY_CSS_SELECTOR =>
        if (multiple) {
          performer.pageReader.findElementsByCssSelector(value)
        } else {
          Seq(getElement(performer.pageReader.findElementByCssSelector(value))(Some(s"$BY_CSS_SELECTOR : $value not found")))
        }
      case BY_NAME =>
        if (multiple) {
          performer.pageReader.findElementsByName(value)
        } else {
          Seq(getElement(performer.pageReader.findElementByName(value))(Some(s"$BY_NAME : $value not found")))
        }

      case BY_PARTIAL_LINK_TEXT =>
        if (multiple) {
          performer.pageReader.findElementsByPartialLinkText(value)
        } else {
          Seq(getElement(performer.pageReader.findElementByPartialLinkText(value))(
            Some(s"$BY_PARTIAL_LINK_TEXT : $value not found")))
        }
      case BY_LINK_TEXT =>
        if (multiple) {
          performer.pageReader.findElementsByLinkText(value)
        } else {
          Seq(getElement(performer.pageReader.findElementByLinkText(value))(Some(s"$BY_LINK_TEXT : $value not found")))
        }
      case BY_CLASS_NAME =>
        if (multiple) {
          performer.pageReader.findElementsByClassName(value)
        } else {
          Seq(getElement(performer.pageReader.findElementByClassName(value))(Some(s"$BY_CLASS_NAME : $value not found")))
        }
      case BY_ID =>
        if (multiple) {
          performer.pageReader.findElementsById(value)
        } else {
          Seq(getElement(performer.pageReader.findElementById(value))(Some(s"$BY_ID : $value not found")))
        }

    }

    f(elems)
  }

  override def perform(actionPerformer: ActionPerformer): Unit = performMultiple {
    TimeActions(pauseBeforeActionMillis).perform(actionPerformer)
    withElements(actionPerformer) { elements =>
      ActionNames.withName(action) match {
        case CLICK => elements.foreach(e => e.click())
        case DOUBLE_CLICK => elements.foreach(e => actionPerformer.doubleClick(e))
        case RIGHT_CLICK => elements.foreach(e => actionPerformer.contextClick(e))
        case CONTEXT_CLICK => elements.foreach(e => actionPerformer.contextClick(e))
        case KEY_DOWN => elements.foreach(e => actionPerformer.keyDown(e, "")) //TODO:: Add charsequence support
        case KEY_UP => elements.foreach(e => actionPerformer.keyUp(e, "")) //TODO:: Add charsequence support
        case SEND_KEYS => elements.foreach(e => actionPerformer.sendKeys(e, "")) //TODO:: Add charsequence support
        case CLICK_AND_HOLD => elements.foreach(e => actionPerformer.clickAndHold(e))
        case RELEASE => elements.foreach(e => actionPerformer.release(e))
        case MOVE_TO_ELEMENT => elements.foreach(e => actionPerformer.moveToElement(e))
        case MOUSE_OVER_ELEMENT => elements.foreach(e => actionPerformer.moveToElement(e))
        case MOVE_BY_OFFSET => elements.foreach(e => actionPerformer.moveByOffset(0, 0)) //TODO:: Add support for offset
        case DRAG_AND_DROP => elements.foreach(e => actionPerformer.dragAndDrop(e, e)) //TODO:: Add support for drag and drop
        case DRAG_AND_DROP_BY => elements.foreach(e => actionPerformer.dragAndDropBy(e, 0, 0)) //TODO:: Add support for offset in drag drop
        case PAUSE => elements.foreach(e => actionPerformer.pause(0)) //TODO:: Add support for pause
      }
    }
  }
}

object FindElementActions {
  implicit val fmt: Format[FindElementActions] = Json.format[FindElementActions]
}
