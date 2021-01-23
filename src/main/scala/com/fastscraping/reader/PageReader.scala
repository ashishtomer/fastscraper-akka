package com.fastscraping.reader

import org.openqa.selenium.firefox.FirefoxDriver

class PageReader(val driver: FirefoxDriver) extends FsJavascriptExecutor with InputDeviceProvider
  with CapabilitiesProvider with ElementFinder with PageInfoProvider with BrowserHandler with TimeoutManager
  with FrameWindowNavigator with CookieManager with BrowserNavigator with WindowHandler {

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

  def getDriver = driver

}

object PageReader {
  def apply(driver: FirefoxDriver): PageReader = new PageReader(driver)
}
