package com.fastscraping.model.actions

import com.fastscraping.model.actions.ActionNames.{CLICK, CLICK_AND_HOLD, CONTEXT_CLICK, DOUBLE_CLICK, MOUSE_OVER_ELEMENT, MOVE_TO_ELEMENT, RELEASE, RIGHT_CLICK}
import com.fastscraping.pageaction.ActionPerformer
import play.api.libs.json.{Format, Json}

case class SelectorActions(action: String,
                           selector: Option[String],
                           times: Int = 1,
                           pauseBeforeActionMillis: Long = 100L) extends Actions {

  override val name = s"Action_${action}_On_$selector"

  override def perform(actionPerformer: ActionPerformer): Unit = performMultiple {
    TimeActions(pauseBeforeActionMillis).perform(actionPerformer)
    withKeyDownChecked(actionPerformer) {
      ActionNames.withName(action) match {
        case CLICK =>
          if (selector.isDefined) {
            actionPerformer.click(selector.get)
          } else {
            actionPerformer.click
          }

        case DOUBLE_CLICK =>
          if (selector.isDefined) {
            actionPerformer.doubleClick(selector.get)
          } else {
            actionPerformer.doubleClick
          }

        case RIGHT_CLICK | CONTEXT_CLICK =>
          if (selector.isDefined) {
            actionPerformer.rightClick(selector.get)
          } else {
            actionPerformer.rightClick
          }

        case CLICK_AND_HOLD =>
          if (selector.isDefined) {
            actionPerformer.clickAndHold(selector.get)
          } else {
            actionPerformer.clickAndHold
          }

        case RELEASE =>
          if (selector.isDefined) {
            actionPerformer.release(selector.get)
          } else {
            actionPerformer.release
          }

        case MOVE_TO_ELEMENT =>
          if (selector.isEmpty) throw new IllegalArgumentException(s"Selector empty for $action")
          actionPerformer.moveToElement(selector.get)

        case MOUSE_OVER_ELEMENT =>
          if (selector.isEmpty) throw new IllegalArgumentException(s"Selector empty for $action")
          actionPerformer.mouseOverElement(selector.get)

      }
    }
  }
}

object SelectorActions {
  implicit val fmt: Format[SelectorActions] = Json.format[SelectorActions]
}
