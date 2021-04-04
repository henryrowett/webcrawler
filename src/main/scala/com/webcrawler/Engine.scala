package com.webcrawler

import cats.effect.std.Queue
import cats.effect.{IO, Ref}
import cats.syntax.all._
import org.jsoup.Jsoup

import scala.jdk.CollectionConverters._

case class Engine(baseUrl: String, repo: Ref[IO, Seq[Webpage]], queue: Queue[IO, Webpage]) {

  // update as required
  var requiredDepth = Depth(0)

  def initStream(webpage: Webpage): fs2.Stream[IO, Unit] =
    fs2.Stream.eval(queue.offer(webpage))

  def crawl(webpage: Webpage): IO[Unit] = {
    val links = getLinks(webpage)
    repo
      .get
      .flatMap { repo =>
        links
          .map(queue.offer)
          .sequence
          .void
          .whenA(!repo.contains(webpage))
    } >>
    repo.update(seq => seq :+ webpage) >>
     IO(println(s"Collected: $webpage, $links"))
  }

  def getLinks(webpage: Webpage): List[Webpage] = {
    try {
      Jsoup
        .connect(webpage.url).get()
        .select("a")
        .asScala
        .toList
        .filter(_.baseUri().contains(baseUrl))
        .map(_.attr("href"))
        .map { href =>
          if (!href.contains("https"))
            Webpage(baseUrl.concat(href.substring(1)))
          else
            Webpage(href)
        }
    } catch {
      case e: Throwable =>
        println(e.getMessage)
        List()
    }
  }

  def processingStream(): fs2.Stream[IO, Unit] =
    fs2.Stream.fromQueueUnterminated(queue,1)
      .evalMap { webpage =>
        IO(println(s"Processing $webpage")) >>
          crawl(webpage).handleErrorWith(e => IO(println(e.getMessage)))
    }
}


