package com.fastscraping.reader

import org.openqa.selenium.interactions.{HasInputDevices, Keyboard, Mouse}
import org.openqa.selenium.remote.RemoteWebDriver

trait InputDeviceProvider extends HasInputDevices {
  def driver: RemoteWebDriver

  override def getKeyboard: Keyboard = driver.getKeyboard
  override def getMouse: Mouse = driver.getMouse
}
