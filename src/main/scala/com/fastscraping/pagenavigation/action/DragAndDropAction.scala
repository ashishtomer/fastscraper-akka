package com.fastscraping.pagenavigation.action

import com.fastscraping.model.{ActionName, Element}
import com.fastscraping.utils.Miscellaneous
import play.api.libs.json.{Format, Json}

case class DragAndDropAction(action: String,
                             fromSelector: String,
                             toSelector: String,
                             times: Option[Int] = Some(1),
                             pauseBeforeActionMillis: Option[Long] = None) extends Action {
  override val name = s"DragAndDrop_From_${fromSelector}_To_$toSelector"

  override def perform(actionPerformer: ActionPerformer)(implicit contextElement: Option[Element]): Unit = {
    Miscellaneous.PrintMetric("performing drag&drop"){
      performMultiple(actionPerformer) {
        withKeyDownChecked(actionPerformer) {
          ActionName.withName(action) match {
            case ActionName.DRAG_AND_DROP => actionPerformer.dragAndDrop(fromSelector, toSelector)
          }
        }
      }
    }
  }
}



object DragAndDropAction {
  implicit val fmt: Format[DragAndDropAction] = Json.format[DragAndDropAction]
}
