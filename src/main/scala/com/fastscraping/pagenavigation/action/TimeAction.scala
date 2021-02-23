package com.fastscraping.pagenavigation.action

import com.fastscraping.model.Element
import com.fastscraping.utils.Miscellaneous
import play.api.libs.json.{Format, Json}

case class TimeAction(pauseMillis: Long,
                      times: Option[Int] = Some(1),
                      pauseBeforeActionMillis: Option[Long] = None) extends Action {
  override val name = s"Pause_ForMillis_$pauseMillis"

  override def perform(actionPerformer: ActionPerformer)(implicit contextElement: Option[Element]): Unit =
    Miscellaneous.PrintMetric("performing timeAction"){
    performMultiple(actionPerformer)(actionPerformer.pause(pauseMillis))
  }
}

object TimeAction {
  implicit val fmt: Format[TimeAction] = Json.format[TimeAction]

  def apply(pauseMillis: Long, times: Option[Int] = Some(1), pauseBeforeActionMillis: Option[Long] = None) = {
    new TimeAction(pauseMillis, times, pauseBeforeActionMillis)
  }
}

object WithPause {
  def apply[A](actionPerformer: ActionPerformer)(f: => A) =  {
    TimeAction(100).perform(actionPerformer)(None)
    f
  }
}
