package com.fastscraping.reader

import org.openqa.selenium.remote.RemoteWebDriver

trait FsJavascriptExecutor {
  def driver: RemoteWebDriver
  def executeScript(script: String, args: Any*): AnyRef = driver.executeScript(script, args)
  def executeAsyncScript(script: String, args: Any*): AnyRef = driver.executeAsyncScript(script, args)
}
