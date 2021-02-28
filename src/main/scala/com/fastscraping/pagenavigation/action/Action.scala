package com.fastscraping.pagenavigation.action

import com.fastscraping.model.Element
import com.fastscraping.pagenavigation.ActionsAndScrape
import com.fastscraping.utils.{FsLogging, JsonParsingException, JsonWriteException}
import org.openqa.selenium.StaleElementReferenceException
import spray.json
import spray.json.{JsValue, RootJsonFormat}

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

  implicit val sprayJsonFmt = new RootJsonFormat[Action] {
    override def read(js: json.JsValue): Action = {
      val jsonFields = js.asJsObject.fields
      if (jsonFields.contains("action") && jsonFields.contains("key")) {
        js.convertTo[KeySelectorAction]
      } else if (jsonFields.contains("action") && jsonFields.contains("findElementBy") && jsonFields.contains("value")) {
        js.convertTo[FindElementAction]
      } else if (jsonFields.contains("action") && jsonFields.contains("xOffset") && jsonFields.contains("yOffset")) {
        js.convertTo[OffsetAction]
      } else if (jsonFields.contains("action") && jsonFields.contains("fromSelector") && jsonFields.contains("toSelector")) {
        js.convertTo[DragAndDropAction]
      }
      else if (jsonFields.contains("action")) {
        js.convertTo[SelectorAction]
      }
      else if (jsonFields.contains("pauseMillis")) {
        js.convertTo[TimeAction]
      }
      else {
        throw JsonParsingException("Action couldn't be parsed", Some(js.prettyPrint))
      }
    }

    override def write(obj: Action): JsValue = obj match {
      case a: SelectorAction => SelectorAction.sprayJsonFmt.write(a)
      case a: KeySelectorAction => KeySelectorAction.sprayJsonFmt.write(a)
      case a: OffsetAction => OffsetAction.sprayJsonFmt.write(a)
      case a: DragAndDropAction => DragAndDropAction.sprayJsonFmt.write(a)
      case a: TimeAction => TimeAction.sprayJsonFmt.write(a)
      case a: FindElementAction => FindElementAction.sprayJsonFmt.write(a)
      case o => throw JsonWriteException(s"$o is not an instance of ${Action.getClass}")
    }
  }
}
