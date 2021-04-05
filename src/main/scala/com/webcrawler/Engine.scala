package com.webcrawler

import cats.effect.std.Queue
import cats.effect.{IO, Ref}
import cats.syntax.all._
import org.jsoup.Jsoup

import scala.jdk.CollectionConverters._

trait Engine[F[_]] {
  def init(webpage: String, subdomain: String, requiredDepth: Integer): fs2.Stream[F, Unit]
}

/** cats.effect.IO implementation of Engine.
  * This implementation uses Ref and Queue from cats effect
  * which are functional, concurrent data structures.
  */
case class IOEngine(repo: Ref[IO, Set[Webpage]], queue: Queue[IO, QueueRecord]) extends Engine[IO] {

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

  def crawl(webpage: Webpage, subdomain: String, depth: Depth, requiredDepth: Depth): IO[Unit] = {
    val links = getLinks(webpage, subdomain)
    links
      .map(w => queue.offer(QueueRecord(w, depth.increment)))
      .sequence
      .void
      .whenA(depth.increment.value <= requiredDepth.value) >>
      repo.update(_ + webpage) >>
      IO(println(s"Collected [Depth: $depth, Webpage: $webpage, Links: $links]"))
  }

  def processor(subdomain: String, requiredDepth: Depth): fs2.Stream[IO, Unit] =
    fs2.Stream
      .fromQueueUnterminated(queue, 1) // this can be updated to process QueueRecords in chunks
      .evalMap { record =>
        repo.get.flatMap { set =>
          {
            IO(println(s"Processing $record")) >>
              crawl(record.webpage, subdomain, record.depth, requiredDepth)
                .handleErrorWith(e => IO(println(e.getMessage)))
          }
            .whenA(!set.contains(record.webpage))
        }
      }

  /** TODO - some links with a redirect that includes the subdomain appear here.
    */
  def getLinks(webpage: Webpage, subdomain: String): List[Webpage] = {
    try {
      Jsoup
        .connect(webpage.value)
        .get()
        .select("a[href]")
        .asScala
        .toList
        .map(_.attr("abs:href"))
        .filter(_.contains(subdomain))
        .map(Webpage)
    } catch {
      // handle error and log to console but don't rethrow to kill program
      case e: Throwable => JsoupErrorHandler.error(e); List();
    }
  }
}
