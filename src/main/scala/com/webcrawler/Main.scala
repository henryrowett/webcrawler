package com.webcrawler

import cats.effect._
import cats.effect.kernel.Resource
import cats.syntax.all._
import cats.effect.std.{Queue => CatsQueue}

object Main extends IOApp {

  override def run(args: List[String]): IO[ExitCode] =
    fs2.Stream
      .resource(resources)
      .flatMap { case (repo, queue) =>
        fs2.Stream(
          Engine("https://monzo.com/", repo, queue).initStream(Webpage("https://monzo.com/")),
            Engine("https://monzo.com/", repo, queue).processingStream(),
            Engine("https://monzo.com/", repo, queue).processingStream())
          .parJoinUnbounded
      }
      .compile
      .drain
      .as(ExitCode.Success)

  val resources: Resource[IO, (Ref[IO, Seq[Webpage]], CatsQueue[IO, Webpage])] =
    (Repo.resource, Queue.resource).tupled
}

case class Webpage(url: String)
case class Depth(value: Integer)
case class Record(webpage: Webpage, links: List[Webpage])