package com.nryanov.neo4s

import org.neo4j.driver.Record

final class Read[A](getters: List[(Get[_], Nullability)], unsafeGet: (Record, Int) => A)

object Read {
  def apply[A](implicit read: Read[A]): read.type = read

  implicit val unit: Read[Unit] = new Read(Nil, (_, _) => ())

  implicit def fromGet[A](implicit get: Get[A]): Read[A] = new Read[A](List((get, NonNull)), get.unsafeGetNonNullable)

  implicit def fromGetNullable[A](implicit get: Get[A]): Read[Option[A]] = new Read[Option[A]](List((get, Nullable)), get.unsafeGetNullable)
}
