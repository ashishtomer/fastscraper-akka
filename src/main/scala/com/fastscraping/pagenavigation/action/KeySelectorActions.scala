package com.fastscraping.pagenavigation.action

import com.fastscraping.model.{ActionNames, Element}
import com.fastscraping.model.ActionNames._
import com.fastscraping.pagenavigation.ActionPerformer
import org.openqa.selenium.Keys
import play.api.libs.json.{Format, Json}


case class KeySelectorActions(action: String,
                              key: String,
                              selector: Option[String],
                              times: Option[Int] = Some(1),
                              pauseBeforeActionMillis: Option[Long] = None) extends Actions {

  override val name = s"Action_${action}_WithKey_$key"

  override def perform(actionPerformer: ActionPerformer)(implicit contextElement: Option[Element]): Unit = {
    TimeActions(pauseBeforeActionMillis.getOrElse(100L)).perform(actionPerformer)
    ActionNames.withName(action) match {
      case KEY_DOWN =>
        if (selector.isDefined) {
          actionPerformer.keyDown(selector.get, key)
        } else {
          actionPerformer.keyDown(key)
        }

        Actions.keyDown = Some(key)

      case KEY_UP =>
        if (selector.isDefined) {
          actionPerformer.keyUp(selector.get, key)
        } else {
          actionPerformer.keyUp(key)
        }

        Actions.keyDown = None

      case SEND_KEYS =>
        if (selector.isDefined) {
          actionPerformer.sendKeys(selector.get, KeySelectorActions.getSeleniumKey(key))
        } else {
          actionPerformer.sendKeys(KeySelectorActions.getSeleniumKey(key))
        }

    }
  }
}

object KeySelectorActions {
  implicit val fmt: Format[KeySelectorActions] = Json.format[KeySelectorActions]

  import Keys._

  def getSeleniumKey(key: String) = key match {
    case "NULL" => NULL
    case "CANCEL" => CANCEL
    case "HELP" => HELP
    case "BACK_SPACE" => BACK_SPACE
    case "TAB" => TAB
    case "CLEAR" => CLEAR
    case "RETURN" => RETURN
    case "ENTER" => ENTER
    case "SHIFT" => SHIFT
    case "LEFT_SHIFT" => LEFT_SHIFT
    case "CONTROL" => CONTROL
    case "LEFT_CONTROL" => LEFT_CONTROL
    case "ALT" => ALT
    case "LEFT_ALT" => LEFT_ALT
    case "PAUSE" => PAUSE
    case "ESCAPE" => ESCAPE
    case "SPACE" => SPACE
    case "PAGE_UP" => PAGE_UP
    case "PAGE_DOWN" => PAGE_DOWN
    case "END" => END
    case "HOME" => HOME
    case "LEFT" => LEFT
    case "ARROW_LEFT" => ARROW_LEFT
    case "UP" => UP
    case "ARROW_UP" => ARROW_UP
    case "RIGHT" => RIGHT
    case "ARROW_RIGHT" => ARROW_RIGHT
    case "DOWN" => DOWN
    case "ARROW_DOWN" => ARROW_DOWN
    case "INSERT" => INSERT
    case "DELETE" => DELETE
    case "SEMICOLON" => SEMICOLON
    case "EQUALS" => EQUALS
    case "NUMPAD0" => NUMPAD0
    case "NUMPAD1" => NUMPAD1
    case "NUMPAD2" => NUMPAD2
    case "NUMPAD3" => NUMPAD3
    case "NUMPAD4" => NUMPAD4
    case "NUMPAD5" => NUMPAD5
    case "NUMPAD6" => NUMPAD6
    case "NUMPAD7" => NUMPAD7
    case "NUMPAD8" => NUMPAD8
    case "NUMPAD9" => NUMPAD9
    case "MULTIPLY" => MULTIPLY
    case "ADD" => ADD
    case "SEPARATOR" => SEPARATOR
    case "SUBTRACT" => SUBTRACT
    case "DECIMAL" => DECIMAL
    case "DIVIDE" => DIVIDE
    case "F1" => F1
    case "F2" => F2
    case "F3" => F3
    case "F4" => F4
    case "F5" => F5
    case "F6" => F6
    case "F7" => F7
    case "F8" => F8
    case "F9" => F9
    case "F10" => F10
    case "F11" => F11
    case "F12" => F12
    case "META" => META
    case "COMMAND" => COMMAND
    case str => str
  }
}

