package com.fastscraping.model.actions

import com.fastscraping.pageaction.ActionPerformer
import play.api.libs.json.{Format, Json}

case class TimeActions(pauseMillis: Long,
                       times: Option[Int] = Some(1),
                       pauseBeforeActionMillis: Option[Long] = None) extends Actions {
  override val name = s"Pause_ForMillis_$pauseMillis"

  override def perform(actionPerformer: ActionPerformer): Unit = performMultiple {
    actionPerformer.pause(pauseMillis)
  }
}

object TimeActions {
  implicit val fmt: Format[TimeActions] = Json.format[TimeActions]
}
