package com.webcrawler

import cats.effect.IO
import com.webcrawler.Main.resources

import scala.concurrent.duration._

class EngineTest extends munit.CatsEffectSuite {

  test("Processing where no record exists") {
    val webpage = Webpage("webpage")
    val prog =
      fs2.Stream
        .resource(resources)
        .flatMap { case (repo, queue) =>
          val process: fs2.Stream[IO, Unit] =
            IOEngine(repo, queue).init("webpage", "www.webpage.com", 10).metered(500.milliseconds)
          val checkRepo: fs2.Stream[IO, Set[Webpage]] =
            fs2.Stream.eval(repo.get.map(_.take(1))).metered(1000.milliseconds)
          checkRepo.concurrently(process)
        }
        .compile
        .toVector

    prog.assertEquals(Vector(Set(webpage)))
  }

  test("No processing of duplicate records") {
    val duplicateWebpage = Webpage("duplicate")
    val prog =
      fs2.Stream
        .resource(resources)
        .flatMap { case (repo, queue) =>
          val process: fs2.Stream[IO, Unit] = fs2.Stream.eval(repo.update(_ + duplicateWebpage)) ++
            IOEngine(repo, queue)
              .init("duplicate", "www.duplicate.com", 10)
              .metered(500.milliseconds)
          val checkRepo: fs2.Stream[IO, Int] =
            fs2.Stream.eval(repo.get.map(_.size)).metered(1000.milliseconds)
          checkRepo.concurrently(process)
        }
        .compile
        .toVector

    prog.assertEquals(Vector(1))
  }

  test("Queue pulls correctly") {
    val webpage = Webpage("webpage")
    val prog =
      fs2.Stream
        .resource(resources)
        .flatMap { case (repo, queue) =>
          val process: fs2.Stream[IO, Unit] =
            fs2.Stream.eval(queue.offer(QueueRecord(webpage, Depth(1)))) ++
              IOEngine(repo, queue).processor(subdomain = "webpage", requiredDepth = Depth(2))
          val checkRepo: fs2.Stream[IO, Set[Webpage]] =
            fs2.Stream.eval(repo.get.map(_.take(1))).metered(1000.milliseconds)
          checkRepo.concurrently(process)
        }
        .compile
        .toVector

    prog.assertEquals(Vector(Set(webpage)))
  }
}
