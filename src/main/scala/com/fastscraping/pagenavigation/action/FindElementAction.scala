package com.fastscraping.pagenavigation.action

import com.fastscraping.model.ActionName._
import com.fastscraping.model.Element
import com.fastscraping.pagenavigation.selenium.ElementFinder.FindElementBy._
import com.fastscraping.utils.Miscellaneous
import org.openqa.selenium.WebElement
import play.api.libs.json.{Format, Json}

case class FindElementAction(action: ActionName,
                             findElementBy: FindElementBy,
                             value: String,
                             multiple: Boolean = false,
                             times: Option[Int] = Some(1),
                             pauseBeforeActionMillis: Option[Long] = None) extends Action {

  override val name = s"${action}_[${findElementBy}_$value]"

  private def withElements[A](performer: ActionPerformer)(f: Seq[WebElement] => A)(implicit contextElement: Option[Element]): A = {

    val elems: Seq[WebElement] = if (multiple) {
      performer.pageReader.findElements(findElementBy, value)
    } else {
      performer.pageReader.findElement(findElementBy, value).map(element => Seq(element))
        .getOrElse {
          logger.error(s"$action not performed. Element not found [$findElementBy=$value][page=${performer.pageReader.currentUrl}]")
          Seq.empty[WebElement]
        }
    }

    f(elems)
  }

  override def perform(actionPerformer: ActionPerformer)(implicit contextElement: Option[Element]): Unit = {
    Miscellaneous.PrintMetric(s"Performing action [findElement=$findElementBy][action=$action][page=${actionPerformer.pageReader.currentUrl}]") {
      withElements(actionPerformer) { elements =>
        action match {
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
}

object FindElementAction {
  implicit val fmt: Format[FindElementAction] = Json.format[FindElementAction]
}
