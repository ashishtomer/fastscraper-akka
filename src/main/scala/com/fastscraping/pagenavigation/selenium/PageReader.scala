package com.fastscraping.pagenavigation.selenium

import org.openqa.selenium.JavascriptExecutor
import org.openqa.selenium.remote.RemoteWebDriver

class PageReader(val driver: RemoteWebDriver) extends InputDeviceProvider with CapabilitiesProvider with ElementFinder
  with PageInfoProvider with BrowserHandler with TimeoutManager with FrameWindowNavigator with CookieManager
  with BrowserNavigator with WindowHandler with FsJavascriptExecutor {

  val jsExecutor = driver.asInstanceOf[JavascriptExecutor]

  def getDriver = driver

  /**
   * Load a new web page in the current browser window. This is done using an HTTP GET operation,
   * and the method will block until the load is complete. This will follow redirects issued either
   * by the server or as a meta-redirect from within the returned HTML. Should a meta-redirect
   * "rest" for any duration of time, it is best to wait until this timeout is over, since should
   * the underlying page change whilst your test is executing the results of future calls against
   * this interface will be against the freshly loaded page. Synonym for
   * {@link org.openqa.selenium.WebDriver.Navigation#to(String)}.
   *
   * @param url The URL to load. It is best to use a fully qualified URL
   */
  def get(url: String) = driver.get(url)
}

object PageReader {
  def apply(driver: RemoteWebDriver): PageReader = new PageReader(driver)
}
