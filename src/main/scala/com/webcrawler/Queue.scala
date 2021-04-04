package com.webcrawler

import cats.effect.IO
import cats.effect.kernel.Resource
import cats.effect.std.{Queue => CatsQueue}

object Queue {
  val resource: Resource[IO, CatsQueue[IO, Webpage]] =
    Resource.eval(CatsQueue.unbounded[IO, Webpage])
}
