package com.fastscraping.reader

import org.openqa.selenium.{Dimension, Point}

trait WindowHandler {
  browserHandler: BrowserHandler =>

  /**
   * Set the size of the current window. This will change the outer window dimension,
   * not just the view port, synonymous to window.resizeTo() in JS.
   *
   * @param targetSize The target size.
   */
  def setSize(targetSize: Dimension): Unit = window.setSize(targetSize)

  /**
   * Set the position of the current window. This is relative to the upper left corner of the
   * screen, synonymous to window.moveTo() in JS.
   *
   * @param targetPosition The target position of the window.
   */
  def setPosition(targetPosition: Point): Unit = window.setPosition(targetPosition)

  /**
   * Get the size of the current window. This will return the outer window dimension, not just
   * the view port.
   *
   * @return The current window size.
   */
  def getSize: Dimension = window.getSize

  /**
   * Get the position of the current window, relative to the upper left corner of the screen.
   *
   * @return The current window position.
   */
  def getPosition: Point = window.getPosition

  /**
   * Maximizes the current window if it is not already maximized
   */
  def maximize(): Unit = window.maximize()

  /**
   * Fullscreen the current window if it is not already fullscreen
   */
  def fullscreen(): Unit = window.fullscreen()
}
