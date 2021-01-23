package com.fastscraping.reader

import java.util.concurrent.TimeUnit

import org.openqa.selenium.WebDriver.Timeouts

trait TimeoutManager {
  browserHandler: BrowserHandler =>

  /**
   * Specifies the amount of time the driver should wait when searching for an element if it is
   * not immediately present.
   * <p>
   * When searching for a single element, the driver should poll the page until the element has
   * been found, or this timeout expires before throwing a {@link NoSuchElementException}. When
   * searching for multiple elements, the driver should poll the page until at least one element
   * has been found or this timeout has expired.
   * <p>
   * Increasing the implicit wait timeout should be used judiciously as it will have an adverse
   * effect on test run time, especially when used with slower location strategies like XPath.
   *
   * @param time The amount of time to wait.
   * @param unit The unit of measure for { @code time}.
   * @return A self reference.
   */
  def implicitlyWait(time: Long, unit: TimeUnit): Timeouts = manage.timeouts().implicitlyWait(time, unit)

  /**
   * Sets the amount of time to wait for an asynchronous script to finish execution before
   * throwing an error. If the timeout is negative, then the script will be allowed to run
   * indefinitely.
   *
   * @param time The timeout value.
   * @param unit The unit of time.
   * @return A self reference.
   * @see JavascriptExecutor#executeAsyncScript(String, Object...)
   */
  def setScriptTimeout(time: Long, unit: TimeUnit): Timeouts = manage.timeouts().setScriptTimeout(time, unit)

  /**
   * Sets the amount of time to wait for a page load to complete before throwing an error.
   * If the timeout is negative, page loads can be indefinite.
   *
   * @param time The timeout value.
   * @param unit The unit of time.
   * @return A Timeouts interface.
   */
  def pageLoadTimeout(time: Long, unit: TimeUnit): Timeouts = manage.timeouts().pageLoadTimeout(time, unit)
}
