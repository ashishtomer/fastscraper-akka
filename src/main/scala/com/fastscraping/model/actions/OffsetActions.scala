package com.fastscraping.model.actions

import com.fastscraping.model.actions.ActionNames.{DRAG_AND_DROP_BY, MOVE_BY_OFFSET, MOVE_TO_ELEMENT}
import com.fastscraping.pageaction.ActionPerformer
import play.api.libs.json.{Format, Json}

case class OffsetActions(action: String,
                         xOffset: Int,
                         yOffset: Int,
                         selector: Option[String],
                         times: Int = 1,
                         pauseBeforeActionMillis: Long = 100L) extends Actions {

  override val name = s"Action_${action}_ByOffset_$xOffset,$yOffset"

  override def perform(actionPerformer: ActionPerformer): Unit = performMultiple {
    TimeActions(pauseBeforeActionMillis).perform(actionPerformer)

    ActionNames.withName(action) match {
      case MOVE_TO_ELEMENT =>
        if (selector.isEmpty) throw new IllegalArgumentException(s"Selector empty for $action")
        actionPerformer.moveToElement(selector.get, xOffset, yOffset)

      case MOVE_BY_OFFSET =>
        actionPerformer.moveByOffset(xOffset, yOffset)

      case DRAG_AND_DROP_BY =>
        if (selector.isEmpty) throw new IllegalArgumentException(s"Selector empty for $action")
        actionPerformer.dragAndDropBy(selector.get, xOffset, yOffset)
    }

  }
}

object OffsetActions {
  implicit val fmt: Format[OffsetActions] = Json.format[OffsetActions]
}
