package neo4s

import org.neo4j.driver.Record
import shapeless._
import shapeless.labelled.{FieldType, field}

final class Read[A](val unsafeGet: (Record, Int) => A)

object Read extends ReadLowerPriority {
  def apply[A](implicit read: Read[A]): read.type = read

  implicit val unit: Read[Unit] = new Read((_, _) => ())

  implicit def fromGet[A](implicit get: Get[A]): Read[A] =
    new Read[A]((record, idx) => get.unsafeGetNonNullable(record.get(idx)))

  implicit def fromGetNullable[A](implicit get: Get[A]): Read[Option[A]] =
    new Read[Option[A]]((record, idx) => get.unsafeGetNullable(record.get(idx)))

  implicit def recordRead[R <: Symbol, H, T <: HList](
    implicit H: Lazy[Read[H]],
    T: Lazy[Read[T]]
  ): Read[FieldType[R, H] :: T] =
    new Read[FieldType[R, H] :: T]((record, idx) => field[R](H.value.unsafeGet(record, idx)) :: T.value.unsafeGet(record, idx + 1))
}

trait ReadLowerPriority extends ReadLowestPriority { this: Read.type =>
  implicit def product[H, T <: HList](
    implicit H: Lazy[Read[H]],
    T: Lazy[Read[T]]
  ): Read[H :: T] =
    new Read[H :: T]((record, idx) => H.value.unsafeGet(record, idx) :: T.value.unsafeGet(record, idx + 1))

  implicit val emptyProduct: Read[HNil] = new Read[HNil]((_, _) => HNil)

  implicit def generic[H, T](implicit gen: Generic.Aux[H, T], T: Lazy[Read[T]]): Read[H] =
    new Read[H]((record, idx) => gen.from(T.value.unsafeGet(record, idx)))
}

trait ReadLowestPriority {
  implicit val emptyOptionProduct: Read[Option[HNil]] = new Read[Option[HNil]]((_, _) => Some(HNil))

  implicit def ohcons1[H, T <: HList](
    implicit H: Lazy[Read[Option[H]]],
    T: Lazy[Read[Option[T]]],
    N: H <:!< Option[α] forSome { type α }
  ): Read[Option[H :: T]] =
    new Read[Option[H :: T]](
      (record, idx) =>
        for {
          h <- H.value.unsafeGet(record, idx)
          t <- T.value.unsafeGet(record, idx + 1)
        } yield h :: t
    )

  implicit def ohcons2[H, T <: HList](
    implicit H: Lazy[Read[Option[H]]],
    T: Lazy[Read[Option[T]]]
  ): Read[Option[Option[H] :: T]] =
    new Read[Option[Option[H] :: T]](
      (record, idx) => T.value.unsafeGet(record, idx + 1).map(H.value.unsafeGet(record, idx) :: _)
    )

  implicit def ogeneric[A, Repr <: HList](
    implicit G: Generic.Aux[A, Repr],
    B: Lazy[Read[Option[Repr]]]
  ): Read[Option[A]] =
    new Read[Option[A]](B.value.unsafeGet(_, _).map(G.from))
}
