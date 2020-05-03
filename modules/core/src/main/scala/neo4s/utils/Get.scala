package neo4s.utils

import neo4s.utils.Get.NonNullableColumnReturnedNull
import org.neo4j.driver.Value
import shapeless._
import shapeless.ops.hlist.IsHCons

import scala.reflect.runtime.universe.TypeTag
import scala.util.control.NoStackTrace

sealed abstract class Get[A](val get: Value => A) {
  def map[B](f: A => B): Get[B]

  final def unsafeGetNonNullable(value: Value): A = {
    if (value.isNull) {
      throw NonNullableColumnReturnedNull
    }
    get(value)
  }

  final def unsafeGetNullable(value: Value): Option[A] =
    if (value.isNull) {
      None
    } else {
      Some(get(value))
    }
}

object Get extends GetInstances {
  def apply[A](implicit get: Get[A]): get.type = get

  case object NonNullableColumnReturnedNull extends RuntimeException("Not nullable column returned null") with NoStackTrace

  final case class Basic[A](override val get: Value => A) extends Get[A](get) {
    override def map[B](f: A => B): Get[B] = copy(get = (value: Value) => f(get(value)))
  }

  implicit def metaProjection[A](implicit meta: Meta[A]): Get[A] = meta.get
}

trait GetInstances {
  implicit def deriveUnaryGet[A: TypeTag, L <: HList, H, T <: HList](
    implicit gen: Generic.Aux[A, L],
    isUnary: (H :: HNil) =:= L,
    C: IsHCons.Aux[L, H, T],
    get: Lazy[Get[H]]
  ): Get[A] =
    get.value.map[A](h => gen.from(h :: HNil))
}
