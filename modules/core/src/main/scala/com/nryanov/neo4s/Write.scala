package com.nryanov.neo4s

import org.neo4j.driver.{Value, Values}

final class Write[A](setters: List[(Put[_], Nullability)], unsafeSet: A => Value) {}

object Write {
  def apply[A](implicit write: Write[A]): write.type = write

  implicit val unit = new Write[Unit](Nil, _ => Values.NULL)

  implicit def fromPut[A](implicit put: Put[A]): Write[A] = new Write[A](List((put, NonNull)), put.unsafeToValueNonNullable)

  implicit def fromPutNullable[A](implicit put: Put[A]): Write[Option[A]] =
    new Write[Option[A]](List((put, Nullable)), put.unsafeToValueNullable)
}
