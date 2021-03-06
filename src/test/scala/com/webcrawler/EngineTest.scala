package com.webcrawler

import cats.effect.IO
import com.webcrawler.Main.resources

import scala.concurrent.duration._

class EngineTest extends munit.CatsEffectSuite {

  test("init adds to queue") {
    val webpage = Webpage("webpage")
    val prog =
      fs2.Stream
        .resource(resources)
        .flatMap { case (repo, queue) =>

          val process: fs2.Stream[IO, Unit] =
            EngineIO(repo, queue).init("webpage", "www.webpage.com", 10).metered(500.milliseconds)

          val checkRepo: fs2.Stream[IO, Set[Webpage]] =
            fs2.Stream.eval(repo.get.map(_.take(1))).metered(1000.milliseconds)

          checkRepo.concurrently(process)
        }
        .compile
        .toVector

    // assert init queues and stores the record
    prog.assertEquals(Vector(Set(webpage)))
  }

  test("No processing of duplicate records") {
    val duplicateWebpage = Webpage("duplicate")
    val webpage2 = Webpage("webpage2")
    val webpage3 = Webpage("webpage3")
    val prog =
      fs2.Stream
        .resource(resources)
        .flatMap { case (repo, queue) =>

          val process: fs2.Stream[IO, Unit] =
          // add 3 webpages to the queue
            fs2.Stream.eval(queue.offer(QueueRecord(duplicateWebpage, Depth(1)))) ++
              fs2.Stream.eval(queue.offer(QueueRecord(webpage2, Depth(1)))) ++
              fs2.Stream.eval(queue.offer(QueueRecord(webpage3, Depth(1)))) ++
          // init with a duplicate webpage which will be added to the queue but not processed
              EngineIO(repo, queue)
                .init("duplicate", "www.duplicate.com", 10)
                .metered(500.milliseconds)

          val checkRepo: fs2.Stream[IO, Int] =
            fs2.Stream.eval(repo.get.map(_.size)).metered(1000.milliseconds)

          checkRepo.concurrently(process)
        }
        .compile
        .toVector

    // assert only 3 records exist
    prog.assertEquals(Vector(3))
  }

  test("Queue pulls correctly") {
    val webpage = Webpage("webpage")
    val webpage2 = Webpage("webpage2")
    val webpage3 = Webpage("webpage3")
    val prog =
      fs2.Stream
        .resource(resources)
        .flatMap { case (repo, queue) =>

          val process: fs2.Stream[IO, Unit] =
            fs2.Stream.eval(queue.offer(QueueRecord(webpage, Depth(1)))) ++
              fs2.Stream.eval(queue.offer(QueueRecord(webpage2, Depth(1)))) ++
              EngineIO(repo, queue)
                .init("webpage3", "www.webpage.com", 10)
                .metered(500.milliseconds)

          val checkRepo: fs2.Stream[IO, Set[Webpage]] =
            fs2.Stream.eval(repo.get.map(_.take(3))).metered(1000.milliseconds)

          checkRepo.concurrently(process)
        }
        .compile
        .toVector

    // assert all webpages are queued and stored
    prog.assertEquals(Vector(Set(webpage, webpage2, webpage3)))
  }
}
