package com.fastscraping.pagenavigation.action

import com.fastscraping.model.{ActionName, Element}
import com.fastscraping.model.ActionName.{DRAG_AND_DROP_BY, MOVE_BY_OFFSET, MOVE_TO_ELEMENT}
import com.fastscraping.utils.Miscellaneous
import play.api.libs.json.{Format, Json}

case class OffsetAction(action: String,
                        xOffset: Int,
                        yOffset: Int,
                        selector: Option[String],
                        times: Option[Int] = Some(1),
                        pauseBeforeActionMillis: Option[Long] = None) extends Action {

  override val name = s"Action_${action}_ByOffset_$xOffset,$yOffset"

  override def perform(actionPerformer: ActionPerformer)(implicit contextElement: Option[Element]): Unit =
    Miscellaneous.PrintMetric(s"performing offsetAction $action") {
      performMultiple(actionPerformer) {

        ActionName.withName(action) match {
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
}

object OffsetAction {
  implicit val fmt: Format[OffsetAction] = Json.format[OffsetAction]
}
