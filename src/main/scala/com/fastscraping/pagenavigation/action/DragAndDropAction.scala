package com.fastscraping.pagenavigation.action

import com.fastscraping.model.{ActionName, Element}
import com.fastscraping.utils.Miscellaneous
import spray.json.DefaultJsonProtocol._
import spray.json.JsonFormat

case class DragAndDropAction(action: String,
                             fromSelector: String,
                             toSelector: String,
                             times: Option[Int] = Some(1),
                             pauseBeforeActionMillis: Option[Long] = None) extends Action {
  override val name = s"DragAndDrop_From_${fromSelector}_To_$toSelector"

  override def perform(actionPerformer: ActionPerformer)(implicit contextElement: Option[Element]): Unit = {
    Miscellaneous.PrintMetric("performing drag&drop") {
      performMultiple(actionPerformer) {
        withKeyDownChecked(actionPerformer) {
          ActionName.withName(action) match {
            case ActionName.DRAG_AND_DROP => actionPerformer.dragAndDrop(fromSelector, toSelector)
            case _ => throw new IllegalArgumentException("Not a drag and action action")
          }
        }
      }
    }
  }

  def toJson = DragAndDropAction.sprayJsonFmt.write(this)

}


object DragAndDropAction {
  implicit val sprayJsonFmt: JsonFormat[DragAndDropAction] = jsonFormat5(DragAndDropAction.apply)
}
