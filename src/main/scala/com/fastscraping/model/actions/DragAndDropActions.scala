package com.fastscraping.model.actions

import com.fastscraping.pageaction.ActionPerformer
import play.api.libs.json.{Format, Json}

case class DragAndDropActions(action: String,
                              fromSelector: String,
                              toSelector: String,
                              times: Option[Int] = Some(1),
                              pauseBeforeActionMillis: Option[Long] = None) extends Actions {
  override val name = s"DragAndDrop_From_${fromSelector}_To_$toSelector"

  override def perform(actionPerformer: ActionPerformer): Unit = performMultiple {
    TimeActions(pauseBeforeActionMillis.getOrElse(100L)).perform(actionPerformer)
    withKeyDownChecked(actionPerformer) {
      ActionNames.withName(action) match {
        case ActionNames.DRAG_AND_DROP => actionPerformer.dragAndDrop(fromSelector, toSelector)
      }
    }
  }
}



object DragAndDropActions {
  implicit val fmt: Format[DragAndDropActions] = Json.format[DragAndDropActions]
}
