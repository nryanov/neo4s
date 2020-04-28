package com.nryanov.neo4s

import com.nryanov.neo4s.Put.NullValueInNotNullablePosition
import org.neo4j.driver.{Value, Values}

import scala.util.control.NoStackTrace

sealed abstract class Put[A](val put: A => Value) {
  def contramap[B](f: B => A): Put[B]

  final def unsafeToValueNonNullable(a: A): Value = {
    if (a == null) throw NullValueInNotNullablePosition
    put(a)
  }

  final def unsafeToValueNullable(a: Option[A]): Value = a match {
    case Some(v) => put(v)
    case None    => Values.NULL
  }
}

object Put {
  def apply[A](implicit put: Put[A]): put.type = put

  case object NullValueInNotNullablePosition extends RuntimeException("Null in not nullable position") with NoStackTrace

  final case class Basic[A](override val put: A => Value) extends Put[A](put) {
    override def contramap[B](f: B => A): Put[B] = copy(put = (b: B) => put(f(b)))
  }

  implicit def metaProjection[A](implicit meta: Meta[A]): Put[A] = meta.put
}
