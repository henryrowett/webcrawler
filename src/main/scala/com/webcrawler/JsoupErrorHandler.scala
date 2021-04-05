package com.webcrawler

import java.net.{MalformedURLException, SocketTimeoutException}

import org.jsoup.UnsupportedMimeTypeException

/** Basic error handling which could be
  * expanded to handle errors/retries.
  */
object JsoupErrorHandler {
  def error(e: Throwable): Unit = e match {
    case e: MalformedURLException => println(s"MalformedURLException: ${e.getMessage}")
    case e: UnsupportedMimeTypeException =>
      println(s"UnsupportedMimeTypeException: ${e.getMessage}")
    case e: SocketTimeoutException => println(s"SocketTimeoutException: ${e.getMessage}")
    case e: Throwable => println(s"Error while processing request: ${e.getMessage}")
  }
}
