package com.webcrawler

import cats.effect.std.Queue
import cats.effect.{IO, Ref}
import cats.syntax.all._
import org.jsoup.Jsoup
import org.typelevel.log4cats.slf4j.Slf4jLogger

import scala.jdk.CollectionConverters._

trait Engine[F[_]] {
  def init(webpage: String, subdomain: String, requiredDepth: Integer): fs2.Stream[F, Unit]
}

/** cats.effect.IO implementation of Engine.
  * This implementation uses Ref and Queue from cats effect
  * which are functional, concurrent data structures.
  */
case class IOEngine(repo: Ref[IO, Set[Webpage]], queue: Queue[IO, QueueRecord]) extends Engine[IO] {

  implicit val log = Slf4jLogger.getLogger[IO]

  def init(webpage: String, subdomain: String, requiredDepth: Integer): fs2.Stream[IO, Unit] =
    fs2
      .Stream(
        fs2.Stream.eval(queue.offer(QueueRecord(Webpage(webpage), Depth(0)))),
        // we can add more processor streams to enable greater concurrent processing.
        // However with too many streams there is some potential for reads/writes
        // to begin blocking and we can sometimes see duplicate records being processed.
        processor(subdomain, Depth(requiredDepth)),
        processor(subdomain, Depth(requiredDepth))
      )
      .parJoin(2)

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

  def processor(subdomain: String, requiredDepth: Depth): fs2.Stream[IO, Unit] =
    fs2.Stream
      .fromQueueUnterminated(queue, 1) // this can be updated to process QueueRecords in chunks
      .evalMap { record =>
        repo.get.flatMap { set =>
          {
            log.info(s"Processing $record") >>
              crawl(record.webpage, subdomain, record.depth, requiredDepth)
                .handleErrorWith(e => log.info(e.getMessage))
          }
            .whenA(!set.contains(record.webpage))
        }
      }

  /** TODO - some links with a redirect that includes the subdomain appear here.
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
