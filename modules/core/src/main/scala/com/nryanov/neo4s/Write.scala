package com.nryanov.neo4s

import org.neo4j.driver.{Value, Values}
import shapeless.labelled.FieldType
import shapeless.{::, <:!<, Generic, HList, HNil, Lazy}

final class Write[A](val unsafeSet: A => Value) {}

object Write extends WriteLowerPriority {
  def apply[A](implicit write: Write[A]): write.type = write

  implicit val unit: Write[Unit] = new Write[Unit](_ => Values.NULL)

  implicit def fromPut[A](implicit put: Put[A]): Write[A] = new Write[A](put.unsafeToValueNonNullable)

  implicit def fromPutNullable[A](implicit put: Put[A]): Write[Option[A]] =
    new Write[Option[A]](put.unsafeToValueNullable)

  implicit def recordWrite[R <: Symbol, H, T <: HList](
    implicit H: Lazy[Write[H]],
    T: Lazy[Write[T]]
  ): Write[FieldType[R, H] :: T] =
    new Write[FieldType[R, H] :: T]({
      case h :: t => H.value.unsafeSet(h); T.value.unsafeSet(t)
    })
}

trait WriteLowerPriority extends WriteLowestPriority { this: Write.type =>
  implicit def product[H, T <: HList](
    implicit H: Lazy[Write[H]],
    T: Lazy[Write[T]]
  ): Write[H :: T] =
    new Write[H :: T]({
      case h :: t => H.value.unsafeSet(h); T.value.unsafeSet(t)
    })

  implicit val emptyProduct: Write[HNil] = new Write[HNil](_ => Values.NULL)

  implicit def generic[H, T](implicit gen: Generic.Aux[H, T], T: Lazy[Write[T]]): Write[H] =
    new Write[H](value => T.value.unsafeSet(gen.to(value)))
}

trait WriteLowestPriority {
  implicit val emptyOptionProduct: Write[Option[HNil]] = new Write[Option[HNil]](_ => Values.NULL)

  implicit def ohcons1[H, T <: HList](
    implicit H: Lazy[Write[Option[H]]],
    T: Lazy[Write[Option[T]]],
    N: H <:!< Option[α] forSome { type α }
  ): Write[Option[H :: T]] = {
    def split[A](v: Option[H :: T])(f: (Option[H], Option[T]) => A): A = v.fold(f(None, None))({ case h :: t => f(Some(h), Some(t)) })

    new Write[Option[H :: T]](
      split(_) {
        case (h, t) => H.value.unsafeSet(h); T.value.unsafeSet(t)
      }
    )
  }

  implicit def ohcons2[H, T <: HList](
    implicit H: Lazy[Write[Option[H]]],
    T: Lazy[Write[Option[T]]]
  ): Write[Option[Option[H] :: T]] = {
    def split[A](v: Option[Option[H] :: T])(f: (Option[H], Option[T]) => A): A = v.fold(f(None, None))({ case h :: t => f(h, Some(t)) })

    new Write[Option[Option[H] :: T]](
      split(_) {
        case (h, t) => H.value.unsafeSet(h); T.value.unsafeSet(t)
      }
    )
  }

  implicit def ogeneric[A, Repr <: HList](
    implicit G: Generic.Aux[A, Repr],
    B: Lazy[Write[Option[Repr]]]
  ): Write[Option[A]] =
    new Write[Option[A]](value => B.value.unsafeSet(value.map(G.to)))
}
