package com.fastscraping.pagenavigation.action

import com.fastscraping.model.Element
import com.fastscraping.pagenavigation.ActionsAndScrape
import com.fastscraping.utils.{FsLogging, JsonParsingException, JsonWriteException}
import org.openqa.selenium.StaleElementReferenceException
import play.api.libs.json._

import scala.util.{Failure, Success, Try}

trait Action extends ActionsAndScrape with FsLogging {

  def perform(actionPerformer: ActionPerformer)(implicit contextElement: Option[Element])

  val times: Option[Int]

  def performMultiple(actionPerformer: ActionPerformer)(f: => Any)(implicit contextElement: Option[Element]): Unit = {
    for (_ <- 0 to times.getOrElse(1)) {
      Try(f) map {
        case Success(unit) => unit
        case Failure(_: StaleElementReferenceException) =>
          logger.error("The element is stale ... retrying action again")
          TimeAction(pauseBeforeActionMillis.getOrElse(5000L)).perform(actionPerformer)
          f
      }
    }
  }

  val pauseBeforeActionMillis: Option[Long]

  def withKeyDownChecked[A](performer: ActionPerformer)(f: => A): A = {
    if (Action.keyDown.isDefined) {
      val result = f
      performer.keyUp(Action.keyDown.get)
      result
    } else {
      f
    }
  }
}

object Action {
  // This is set to Some(key) when KEY_DOWN is invoked.
  // It'll be set to None upon the adjacent action like CLICK, DOUBLE_CLICK etc
  var keyDown: Option[String] = None

  implicit val reads: Reads[Action] = (json: JsValue) => try {
    JsSuccess {
      val jsonFields = json.as[JsObject].keys
      if (jsonFields.contains("action") && jsonFields.contains("key")) {
        json.as[KeySelectorAction]
      } else if (jsonFields.contains("action") && jsonFields.contains("findElementBy") && jsonFields.contains("value")) {
        json.as[FindElementAction]
      } else if (jsonFields.contains("action") && jsonFields.contains("xOffset") && jsonFields.contains("yOffset")) {
        json.as[OffsetAction]
      } else if (jsonFields.contains("action") && jsonFields.contains("fromSelector") && jsonFields.contains("toSelector")) {
        json.as[DragAndDropAction]
      }
      else if (jsonFields.contains("action")) {
        json.as[SelectorAction]
      }
      else if (jsonFields.contains("pauseMillis")) {
        json.as[TimeAction]
      }
      else {
        throw JsonParsingException("Action couldn't be parsed", Some(Json.prettyPrint(json)))
      }
    }
  } catch {
    case ex: JsonParsingException if ex.getMessage.contains("Action couldn't be parsed") => JsError()
  }

  implicit val writes: Writes[Action] = {
    case a: SelectorAction => Json.toJson(a)
    case a: KeySelectorAction => Json.toJson(a)
    case a: OffsetAction => Json.toJson(a)
    case a: DragAndDropAction => Json.toJson(a)
    case a: TimeAction => Json.toJson(a)
    case a: FindElementAction => Json.toJson(a)
    case o => throw JsonWriteException(s"$o is not an instance of ${Writes.getClass}")
  }

  implicit val fmt: Format[Action] = Format(reads, writes)
}
