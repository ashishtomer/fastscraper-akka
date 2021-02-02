package com.fastscraping.pagenavigation.selenium

import org.openqa.selenium.Cookie
import scala.jdk.CollectionConverters._

trait CookieManager {
  browserHandler: BrowserHandler =>


  /**
   * Add a specific cookie. If the cookie's domain name is left blank, it is assumed that the
   * cookie is meant for the domain of the current document.
   *
   * @param cookie The cookie to add.
   */
  def addCookie(cookie: Cookie): Unit = manage.addCookie(cookie)

  /**
   * Delete the named cookie from the current domain. This is equivalent to setting the named
   * cookie's expiry date to some time in the past.
   *
   * @param name The name of the cookie to delete
   */
  def deleteCookieNamed(name: String): Unit = manage.deleteCookieNamed(name)

  /**
   * Delete a cookie from the browser's "cookie jar". The domain of the cookie will be ignored.
   *
   * @param cookie nom nom nom
   */
  def deleteCookie(cookie: Cookie): Unit = manage.deleteCookie(cookie)

  /**
   * Delete all the cookies for the current domain.
   */
  def deleteAllCookies(): Unit = manage.deleteAllCookies()

  /**
   * Get all the cookies for the current domain. This is the equivalent of calling
   * "document.cookie" and parsing the result
   *
   * @return A Set of cookies for the current domain.
   */
  def getCookies: Set[Cookie] = manage.getCookies.asScala.toSet

  /**
   * Get a cookie with a given name.
   *
   * @param name the name of the cookie
   * @return the cookie, or null if no cookie with the given name is present
   */
  def getCookieNamed(name: String): Cookie = manage.getCookieNamed(name)
}
