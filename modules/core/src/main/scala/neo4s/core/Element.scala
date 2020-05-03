package neo4s.core

import neo4s.utils.Put

sealed trait Element

object Element {
  final case class Arg[A](a: A, put: Put[A]) extends Element

  final case class Opt[A](a: Option[A], put: Put[A]) extends Element

  implicit def fromArg[A](a: A)(implicit put: Put[A]): Element = Element.Arg(a, put)

  implicit def fromOpt[A](a: Option[A])(implicit put: Put[A]): Element = Element.Opt(a, put)
}
