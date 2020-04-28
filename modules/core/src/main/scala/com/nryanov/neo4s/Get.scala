package com.nryanov.neo4s

import com.nryanov.neo4s.Get.NonNullableColumnReturnedNull
import org.neo4j.driver.{Record, Value}

import scala.util.control.NoStackTrace

sealed abstract class Get[A](val get: (Record, Int) => A) {
  def map[B](f: A => B): Get[B]

  final def unsafeGetNonNullable(record: Record, idx: Int): A = {
    if (record.get(idx).isNull) {
      throw NonNullableColumnReturnedNull
    }
    get(record, idx)
  }

  final def unsafeGetNullable(record: Record, idx: Int): Option[A] =
    if (record.get(idx).isNull) {
      None
    } else {
      Some(get(record, idx))
    }
}

object Get {
  def apply[A](implicit get: Get[A]): get.type = get

  case object NonNullableColumnReturnedNull extends RuntimeException("Not nullable column returned null") with NoStackTrace

  final case class Basic[A](override val get: (Record, Int) => A) extends Get[A](get) {
    override def map[B](f: A => B): Get[B] = copy(get = (record: Record, idx: Int) => f(get(record, idx)))
  }

  implicit def metaProjection[A](implicit meta: Meta[A]): Get[A] = meta.get
}
