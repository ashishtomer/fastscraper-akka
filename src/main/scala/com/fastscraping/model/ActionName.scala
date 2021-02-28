package com.fastscraping.model

import com.fastscraping.utils.EnumSprayJsonFormat

object ActionName extends Enumeration {
  type ActionName = Value
  val CLICK, DOUBLE_CLICK, RIGHT_CLICK, CONTEXT_CLICK, KEY_DOWN, KEY_UP, SEND_KEYS, CLICK_AND_HOLD, RELEASE,
  MOVE_TO_ELEMENT, MOUSE_OVER_ELEMENT, MOVE_BY_OFFSET, DRAG_AND_DROP, DRAG_AND_DROP_BY, PAUSE = Value

  implicit val sprayFmt = EnumSprayJsonFormat(ActionName)
}
