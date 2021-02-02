package com.fastscraping.model

object ActionNames extends Enumeration {
  type ActionNames = Value
  val CLICK, DOUBLE_CLICK, RIGHT_CLICK, CONTEXT_CLICK, KEY_DOWN, KEY_UP, SEND_KEYS, CLICK_AND_HOLD, RELEASE,
  MOVE_TO_ELEMENT, MOUSE_OVER_ELEMENT, MOVE_BY_OFFSET, DRAG_AND_DROP, DRAG_AND_DROP_BY, PAUSE = Value
}
