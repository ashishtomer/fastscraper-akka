package com.fastscraping.reader

import org.openqa.selenium.{Alert, NoAlertPresentException, NoSuchFrameException, NoSuchWindowException, StaleElementReferenceException, WebDriver, WebElement}

trait FrameWindowNavigator {
  browserHandler: BrowserHandler =>

  /**
   * Select a frame by its (zero-based) index. Selecting a frame by index is equivalent to the
   * JS expression window.frames[index] where "window" is the DOM window represented by the
   * current context. Once the frame has been selected, all subsequent calls on the WebDriver
   * interface are made to that frame.
   *
   * @param index (zero-based) index
   * @return This driver focused on the given frame
   * @throws NoSuchFrameException If the frame cannot be found
   */
  def frame(index: Int): WebDriver = switchTo.frame(index)

  /**
   * Select a frame by its name or ID. Frames located by matching name attributes are always given
   * precedence over those matched by ID.
   *
   * @param nameOrId the name of the frame window, the id of the &lt;frame&gt; or &lt;iframe&gt;
   *                 element, or the (zero-based) index
   * @return This driver focused on the given frame
   * @throws NoSuchFrameException If the frame cannot be found
   */
  def frame(nameOrId: String): WebDriver = switchTo.frame(nameOrId)

  /**
   * Select a frame using its previously located {@link WebElement}.
   *
   * @param frameElement The frame element to switch to.
   * @return This driver focused on the given frame.
   * @throws NoSuchFrameException           If the given element is neither an IFRAME nor a FRAME element.
   * @throws StaleElementReferenceException If the WebElement has gone stale.
   * @see WebDriver#findElement(By)
   */
  def frame(frameElement: WebElement): WebDriver = switchTo.frame(frameElement)

  /**
   * Change focus to the parent context. If the current context is the top level browsing context,
   * the context remains unchanged.
   *
   * @return This driver focused on the parent frame
   */
  def parentFrame: WebDriver = switchTo.parentFrame()

  /**
   * Switch the focus of future commands for this driver to the window with the given name/handle.
   *
   * @param nameOrHandle The name of the window or the handle as returned by
   *                     { @link WebDriver#getWindowHandle()}
   * @return This driver focused on the given window
   * @throws NoSuchWindowException If the window cannot be found
   */
  def window(nameOrHandle: String): WebDriver = switchTo.window(nameOrHandle)

  /**
   * Selects either the first frame on the page, or the main document when a page contains
   * iframes.
   *
   * @return This driver focused on the top window/first frame.
   */
  def defaultContent: WebDriver = switchTo.defaultContent()

  /**
   * Switches to the element that currently has focus within the document currently "switched to",
   * or the body element if this cannot be detected. This matches the semantics of calling
   * "document.activeElement" in Javascript.
   *
   * @return The WebElement with focus, or the body element if no element with focus can be
   *         detected.
   */
  def activeElement: WebElement = switchTo.activeElement()

  /**
   * Switches to the currently active modal dialog for this particular driver instance.
   *
   * @return A handle to the dialog.
   * @throws NoAlertPresentException If the dialog cannot be found
   */
  def alert: Alert = switchTo.alert()
}
