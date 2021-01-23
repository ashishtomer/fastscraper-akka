package com.fastscraping.reader

import com.google.common.annotations.Beta
import org.openqa.selenium.Cookie
import org.openqa.selenium.WebDriver.{ImeHandler, Navigation, Options, TargetLocator, Timeouts, Window}
import org.openqa.selenium.logging.Logs
import org.openqa.selenium.remote.RemoteWebDriver

import scala.jdk.CollectionConverters._

trait BrowserHandler {

  def driver: RemoteWebDriver

  /**
   * Close the current window, quitting the browser if it's the last window currently open.
   */
  def close(): Unit = driver.close()

  /**
   * Quits this driver, closing every associated window.
   */
  def quit(): Unit = driver.close()

  /**
   * Return a set of window handles which can be used to iterate over all open windows of this
   * WebDriver instance by passing them to {@link #switchTo()}.{@link Options#window()}
   *
   * @return A set of window handles which can be used to iterate over all open windows.
   */
  def getWindowHandles: Set[String] = driver.getWindowHandles.asScala.toSet

  /**
   * Return an opaque handle to this window that uniquely identifies it within this driver instance.
   * This can be used to switch to this window at a later date
   *
   * @return the current window handle
   */
  def getWindowHandle: String = driver.getWindowHandle

  /**
   * Send future commands to a different frame or window.
   *
   * @return A TargetLocator which can be used to select a frame or window
   * @see org.openqa.selenium.WebDriver.TargetLocator
   */
  def switchTo: TargetLocator = driver.switchTo()

  /**
   * An abstraction allowing the driver to access the browser's history and to navigate to a given
   * URL.
   *
   * @return A { @link org.openqa.selenium.WebDriver.Navigation} that allows the selection of what to
   *                   do next
   */
  def navigate: Navigation = driver.navigate()

  /**
   * Gets the Option interface
   *
   * @return An option interface
   * @see org.openqa.selenium.WebDriver.Options
   */
  def manage: Options = driver.manage()

  /**
   * @return the interface for managing driver timeouts.
   */
  def timeouts: Timeouts = manage.timeouts()

  /**
   * @return the interface for controlling IME engines to generate complex-script input.
   */
  def ime: ImeHandler = manage.ime()

  /**
   * @return the interface for managing the current window.
   */
  def window: Window = manage.window()

  /**
   * Gets the {@link Logs} interface used to fetch different types of logs.
   *
   * <p>To set the logging preferences {@link LoggingPreferences}.
   *
   * @return A Logs interface.
   */
  @Beta def logs: Logs = manage.logs()
}
