package com.webcrawler

import cats.effect.IO
import cats.effect.kernel.{Ref, Resource}

trait Repo[F[_], A] {
  def put(a: A): F[Unit]
}

object Repo {
  val resource: Resource[IO, Ref[IO, Seq[Webpage]]] =
    Resource.eval(Ref[IO].of(Seq[Webpage]()))
}
