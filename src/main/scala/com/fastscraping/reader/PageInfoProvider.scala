package com.fastscraping.reader

import org.openqa.selenium.remote.RemoteWebDriver

trait PageInfoProvider {
  def driver: RemoteWebDriver

  /**
   * Get a string representing the current URL that the browser is looking at.
   *
   * @return The URL of the page currently loaded in the browser
   */
  def getCurrentUrl: String = driver.getCurrentUrl

  // General properties

  /**
   * The title of the current page.
   *
   * @return The title of the current page, with leading and trailing whitespace stripped, or null
   *         if one is not already set
   */
  def getTitle: String = driver.getTitle

  /**
   * Get the source of the last loaded page. If the page has been modified after loading (for
   * example, by Javascript) there is no guarantee that the returned text is that of the modified
   * page. Please consult the documentation of the particular driver being used to determine whether
   * the returned text reflects the current state of the page or the text last sent by the web
   * server. The page source returned is a representation of the underlying DOM: do not expect it to
   * be formatted or escaped in the same way as the response sent from the web server. Think of it as
   * an artist's impression.
   *
   * @return The source of the current page
   */
  def getPageSource: String = driver.getPageSource
}
