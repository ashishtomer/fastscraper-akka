package com.fastscraping.pagenavigation.action

import com.fastscraping.model.Element
import com.fastscraping.pagenavigation.ActionPerformer
import com.fastscraping.utils.Miscellaneous
import play.api.libs.json.{Format, Json}

case class TimeActions(pauseMillis: Long,
                       times: Option[Int] = Some(1),
                       pauseBeforeActionMillis: Option[Long] = None) extends Actions {
  override val name = s"Pause_ForMillis_$pauseMillis"

  override def perform(actionPerformer: ActionPerformer)(implicit contextElement: Option[Element]): Unit =
    Miscellaneous.PrintMetric("performing timeAction"){
    performMultiple(actionPerformer)(actionPerformer.pause(pauseMillis))
  }
}

object TimeActions {
  implicit val fmt: Format[TimeActions] = Json.format[TimeActions]

  def apply(pauseMillis: Long, times: Option[Int] = Some(1), pauseBeforeActionMillis: Option[Long] = None) = {
    new TimeActions(pauseMillis, times, pauseBeforeActionMillis)
  }
}

object WithPause {
  def apply[A](actionPerformer: ActionPerformer)(f: => A) =  {
    TimeActions(100).perform(actionPerformer)(None)
    f
  }
}
