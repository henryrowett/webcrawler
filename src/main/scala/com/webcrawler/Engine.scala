package com.webcrawler

import cats.effect.std.Queue
import cats.effect.{IO, Ref}
import cats.syntax.all._
import org.jsoup.Jsoup
import org.typelevel.log4cats.slf4j.Slf4jLogger

import scala.jdk.CollectionConverters._

trait Engine[F[_]] {
  def init(
      webpage: String,
      subdomain: String,
      requiredDepth: Integer,
      numberOfConcurrentProcessors: Integer = 5
  ): fs2.Stream[F, Unit]
}

/** cats.effect.IO implementation of Engine.
  * This implementation uses Ref and Queue from cats effect
  * which are concurrent data structures.
  */
case class EngineIO(repo: Ref[IO, Set[Webpage]], queue: Queue[IO, QueueRecord]) extends Engine[IO] {

  implicit val log = Slf4jLogger.getLogger[IO]

  def init(
      webpage: String,
      subdomain: String,
      requiredDepth: Integer,
      numberOfConcurrentProcessors: Integer = 5
  ): fs2.Stream[IO, Unit] =
    fs2
      .Stream(
        fs2.Stream.eval(queue.offer(QueueRecord(Webpage(webpage), Depth(0)))),
          processingEngines(subdomain, requiredDepth, numberOfConcurrentProcessors)
      )
      .parJoinUnbounded

  private def processingEngines(
      subdomain: String,
      requiredDepth: Integer,
      numberOfConcurrentProcessors: Integer
  ) = {
    fs2
      .Stream(processor(subdomain, Depth(requiredDepth)))
      .repeatN(numberOfConcurrentProcessors.toLong)
      .parJoinUnbounded
  }

  /**
    * Pulls from the queue and processes the link if
    * it has not yet been processed.
    * This can be updated to pull in chunks if required.
    */
  def processor(subdomain: String, requiredDepth: Depth): fs2.Stream[IO, Unit] =
    fs2.Stream
      .fromQueueUnterminated(queue, 1) // this can be updated to process QueueRecords in chunks
      .evalMap { record =>
        repo.get.flatMap { set =>
          {
            log.info(s"Processing $record") >>
              crawl(record.webpage, subdomain, record.depth, requiredDepth)
                .handleErrorWith(e => log.info(e.getMessage))
          }.whenA(!set.contains(record.webpage))
        }
      }

  /**
    * Adds valid links to the queue when the depth has not
    * exceeded the required depth then adds the crawled link
    * to the repo so it is not duplicated.
    */
  def crawl(webpage: Webpage, subdomain: String, depth: Depth, requiredDepth: Depth): IO[Unit] =
  getLinks(webpage, subdomain).flatMap { links =>
      links
          .map(w => queue.offer(QueueRecord(w, depth.increment)))
          .sequence
          .void
          .whenA(depth.increment.value <= requiredDepth.value) >>
      repo.update(_ + webpage) >>
      log.info(s"Collected [Webpage: $webpage, Depth: $depth, Links: $links]")
    }


  /**
    * Grabs all valid links from the page and returns them.
    * In some cases, if a link is a redirect to the specified
    * domain then it can be included even if it isn't 'valid'.
    */
  def getLinks(webpage: Webpage, subdomain: String): IO[List[Webpage]] = {
    IO(
      Jsoup
        .connect(webpage.value)
        .get()
        .select("a[href]")
        .asScala
        .toList
        .map(_.attr("abs:href"))
        .filter(_.contains(subdomain))
        .map(Webpage)
    ).handleErrorWith(e => JsoupErrorHandler.error(e) >> IO(List()))
  }
}
