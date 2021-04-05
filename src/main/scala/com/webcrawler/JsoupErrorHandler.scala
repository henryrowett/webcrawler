package com.webcrawler

import java.net.{MalformedURLException, SocketTimeoutException}

import cats.effect.IO
import org.jsoup.UnsupportedMimeTypeException
import org.typelevel.log4cats.slf4j.Slf4jLogger

/** Basic error handling which could be
  * expanded to handle errors/retries.
  */
object JsoupErrorHandler {
  implicit val log = Slf4jLogger.getLogger[IO]

  def error(e: Throwable): IO[Unit] = e match {
    case e: MalformedURLException => log.error(s"MalformedURLException: ${e.getMessage}")
    case e: UnsupportedMimeTypeException =>
      log.error(s"UnsupportedMimeTypeException: ${e.getMessage}")
    case e: SocketTimeoutException => log.error(s"SocketTimeoutException: ${e.getMessage}")
    case e: Throwable => log.error(s"Error while processing request: ${e.getMessage}")
  }
}
