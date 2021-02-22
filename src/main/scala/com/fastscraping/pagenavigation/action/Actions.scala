package com.fastscraping.pagenavigation.action

import com.fastscraping.model.Element
import com.fastscraping.pagenavigation.{ActionPerformer, ActionsAndScrape}
import com.fastscraping.utils.{FsLogging, JsonParsingException, JsonWriteException, Miscellaneous}
import org.openqa.selenium.StaleElementReferenceException
import play.api.libs.json._

import scala.util.{Failure, Success, Try}

trait Actions extends ActionsAndScrape with FsLogging {

  def perform(actionPerformer: ActionPerformer)(implicit contextElement: Option[Element])

  val times: Option[Int]

  def performMultiple(actionPerformer: ActionPerformer)(f: => Any)(implicit contextElement: Option[Element]): Unit = {
    Miscellaneous.PrintMetric("performing action"){
      for (_ <- 0 to times.getOrElse(1)) {
        Try(f) map {
          case Success(unit) => unit
          case Failure(_: StaleElementReferenceException) =>
            logger.error("The element is stale ... retrying action again")
            TimeActions(pauseBeforeActionMillis.getOrElse(5000L)).perform(actionPerformer)
            f
        }
      }
    }
  }

  val pauseBeforeActionMillis: Option[Long]

  def withKeyDownChecked[A](performer: ActionPerformer)(f: => A): A = {
    if (Actions.keyDown.isDefined) {
      val result = f
      performer.keyUp(Actions.keyDown.get)
      result
    } else {
      f
    }
  }
}

object Actions {
  // This is set to Some(key) when KEY_DOWN is invoked.
  // It'll be set to None upon the adjacent action like CLICK, DOUBLE_CLICK etc
  var keyDown: Option[String] = None

  implicit val reads: Reads[Actions] = (json: JsValue) => try {
    JsSuccess {
      val jsonFields = json.as[JsObject].keys
      if (jsonFields.contains("action") && jsonFields.contains("key")) {
        json.as[KeySelectorActions]
      } else if (jsonFields.contains("action") && jsonFields.contains("findElementBy") && jsonFields.contains("value")) {
        json.as[FindElementActions]
      } else if (jsonFields.contains("action") && jsonFields.contains("xOffset") && jsonFields.contains("yOffset")) {
        json.as[OffsetActions]
      } else if (jsonFields.contains("action") && jsonFields.contains("fromSelector") && jsonFields.contains("toSelector")) {
        json.as[DragAndDropActions]
      }
      else if (jsonFields.contains("action")) {
        json.as[SelectorActions]
      }
      else if (jsonFields.contains("pauseMillis")) {
        json.as[TimeActions]
      }
      else {
        throw JsonParsingException("Action couldn't be parsed", Some(Json.prettyPrint(json)))
      }
    }
  } catch {
    case ex: JsonParsingException if ex.getMessage.contains("Action couldn't be parsed") => JsError()
  }

  implicit val writes: Writes[Actions] = {
    case a: SelectorActions => Json.toJson(a)
    case a: KeySelectorActions => Json.toJson(a)
    case a: OffsetActions => Json.toJson(a)
    case a: DragAndDropActions => Json.toJson(a)
    case a: TimeActions => Json.toJson(a)
    case a: FindElementActions => Json.toJson(a)
    case o => throw JsonWriteException(s"$o is not an instance of ${Writes.getClass}")
  }

  implicit val fmt: Format[Actions] = Format(reads, writes)
}
