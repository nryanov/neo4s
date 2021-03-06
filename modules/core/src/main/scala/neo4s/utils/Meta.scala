package neo4s.utils

import java.time._
import java.util
import java.util.UUID

import org.neo4j.driver.{Value, Values}

import neo4s.utils.CollectionCompat._
import scala.reflect.ClassTag

final case class Meta[A](get: Get[A], put: Put[A]) {
  def imap[B](a: A => B)(b: B => A): Meta[B] =
    Meta(get.map(a), put.contramap(b))
}

object Meta extends MetaInstances {
  def apply[A](implicit meta: Meta[A]): meta.type = meta

  def basic[A](get: Value => A, put: A => Value) = new Meta[A](Get.Basic(get), Put.Basic(put))
}

trait MetaInstances {

  implicit val byteMetaInstance: Meta[Byte] = Meta.basic[Byte](_.asInt().toByte, byte => Values.value(byte.toInt))

  implicit val shortMetaInstance: Meta[Short] = Meta.basic[Short](_.asInt().toShort, short => Values.value(short.toInt))

  implicit val intMetaInstance: Meta[Int] = Meta.basic[Int](_.asInt(), int => Values.value(int))

  implicit val longMetaInstance: Meta[Long] = Meta.basic[Long](_.asLong(), long => Values.value(long))

  implicit val floatMetaInstance: Meta[Float] = Meta.basic[Float](_.asFloat(), float => Values.value(float.toDouble))

  implicit val doubleMetaInstance: Meta[Double] = Meta.basic[Double](_.asDouble(), double => Values.value(double))

  implicit val characterMetaInstance: Meta[Char] = Meta.basic[Char](_.asInt().toChar, char => Values.value(char.toInt))

  implicit val stringMetaInstance: Meta[String] = Meta.basic[String](_.asString(), string => Values.value(string))

  implicit val booleanMetaInstance: Meta[Boolean] = Meta.basic[Boolean](_.asBoolean(), bool => Values.value(bool))

  implicit val uuidMetaInstance: Meta[UUID] = Meta[String].imap(str => UUID.fromString(str))(uuid => uuid.toString)

  // neo4j driver does not support BigInt values.
  implicit val bigIntMetaInstance: Meta[BigInt] = Meta[String].imap(str => BigInt.apply(str))(value => value.toString())

  // neo4j driver does not support BigDecimal values.
  implicit val bigDecimalMetaInstance: Meta[BigDecimal] = Meta[String].imap(str => BigDecimal.apply(str))(value => value.toString())

  implicit val offsetTimeMetaInstance: Meta[OffsetTime] = Meta.basic[OffsetTime](_.asOffsetTime(), offsetTime => Values.value(offsetTime))

  implicit val localDateMetaInstance: Meta[LocalDate] = Meta.basic[LocalDate](_.asLocalDate(), localDate => Values.value(localDate))

  implicit val localDateTimeMetaInstance: Meta[LocalDateTime] =
    Meta.basic[LocalDateTime](_.asLocalDateTime(), localDateTime => Values.value(localDateTime))

  implicit val localTimeMetaInstance: Meta[LocalTime] = Meta.basic[LocalTime](_.asLocalTime(), localTime => Values.value(localTime))

  implicit val offsetDateTimeMetaInstance: Meta[OffsetDateTime] =
    Meta.basic[OffsetDateTime](_.asOffsetDateTime(), offsetDateTime => Values.value(offsetDateTime))

  implicit val zonedDateTimeMetaInstance: Meta[ZonedDateTime] =
    Meta.basic[ZonedDateTime](_.asZonedDateTime(), zonedDateTime => Values.value(zonedDateTime))

  implicit val byteArrayMetaInstance: Meta[Array[Byte]] = Meta.basic[Array[Byte]](_.asByteArray(), byteArray => Values.value(byteArray))

  implicit def listMetaInstance[A: Get: Put: ClassTag]: Meta[List[A]] =
    Meta.basic[List[A]](value => value.asList[A](Get[A].get(_)), list => Values.value(list.map(Put[A].put): _*))

  implicit def arrayMetaInstance[A: Get: Put: ClassTag]: Meta[Array[A]] = Meta[List[A]].imap(_.toArray)(_.toList)

  implicit def mapMetaInstance[A: Get: Put: ClassTag]: Meta[Map[String, A]] =
    Meta.basic[Map[String, A]](
      value => value.asMap[A](Get[A].get(_)),
      map => {
        val mapOfValues: Map[String, Value] = mapValues(map, Put[A].put)
        val javaMap: util.Map[String, Value] = mapOfValues
        Values.value(javaMap)
      }
    )
}
