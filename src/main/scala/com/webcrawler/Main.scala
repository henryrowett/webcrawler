package com.webcrawler

import cats.effect._
import cats.effect.kernel.{Ref, Resource}
import cats.syntax.all._
import cats.effect.std.{Queue => CatsQueue}

object Main extends IOApp {

  override def run(args: List[String]): IO[ExitCode] =
    fs2.Stream
      .resource(resources)
      .flatMap { case (repo, queue) =>
        EngineIO(repo, queue)
          .init(
            webpage = "https://monzo.com/",
            subdomain = "https://monzo.com/",
            requiredDepth = 2,
            numberOfConcurrentProcessors = 2
          )
      }
      .compile
      .drain
      .as(ExitCode.Success)

  val resources: Resource[IO, (Ref[IO, Set[Webpage]], CatsQueue[IO, QueueRecord])] =
    (Repo.resource, Queue.resource).tupled
}

/**
  * CatsQueue => A purely functional, concurrent data structure which allows insertion and
  * retrieval of elements of type `A` in a first-in-first-out (FIFO) manner.
  */
object Queue {
  val resource: Resource[IO, CatsQueue[IO, QueueRecord]] =
    Resource.eval(CatsQueue.unbounded[IO, QueueRecord])
}

/**
  * Prevents duplicate processing.
  *  Ref => An asynchronous, concurrent mutable reference.
  */
object Repo {
  val resource: Resource[IO, Ref[IO, Set[Webpage]]] =
    Resource.eval(Ref[IO].of(Set[Webpage]()))
}

case class Webpage(value: String)
case class QueueRecord(webpage: Webpage, depth: Depth)
case class Depth(value: Integer) {
  def increment: Depth = Depth(value + 1)
}

