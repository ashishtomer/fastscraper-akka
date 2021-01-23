package com.fastscraping.reader

import org.openqa.selenium.remote.RemoteWebDriver
import org.openqa.selenium.{Capabilities, HasCapabilities}

trait CapabilitiesProvider extends HasCapabilities {
  def driver: RemoteWebDriver
  override def getCapabilities: Capabilities = driver.getCapabilities
}
