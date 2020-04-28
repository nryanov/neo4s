package com.nryanov.neo4s

import java.time.{LocalDate, LocalDateTime, LocalTime, OffsetDateTime, OffsetTime, ZonedDateTime}
import java.util

import org.neo4j.driver.{Record, Value, Values}

import scala.jdk.CollectionConverters._
import scala.reflect.ClassTag

final case class Meta[A](get: Get[A], put: Put[A]) {
  def imap[B](a: A => B)(b: B => A): Meta[B] =
    Meta(get.map(a), put.contramap(b))
}

object Meta {
  def apply[A](implicit meta: Meta[A]): meta.type = meta

  def basic[A](get: (Record, Int) => A, put: A => Value) = new Meta[A](Get.Basic(get), Put.Basic(put))
}

trait MetaInstances {

  implicit val byteMetaInstance: Meta[Byte] =
    Meta.basic[Byte]((record, idx) => record.get(idx).asInt().byteValue, byte => Values.value(byte.toInt))

  implicit val shortMetaInstance: Meta[Short] =
    Meta.basic[Short]((record, idx) => record.get(idx).asInt().shortValue, short => Values.value(short.toInt))

  implicit val intMetaInstance: Meta[Int] = Meta.basic[Int]((record, idx) => record.get(idx).asInt(), int => Values.value(int))

  implicit val longMetaInstance: Meta[Long] = Meta.basic[Long]((record, idx) => record.get(idx).asLong(), long => Values.value(long))

  implicit val floatMetaInstance: Meta[Float] =
    Meta.basic[Float]((record, idx) => record.get(idx).asFloat(), float => Values.value(float.toDouble))

  implicit val doubleMetaInstance: Meta[Double] =
    Meta.basic[Double]((record, idx) => record.get(idx).asDouble(), double => Values.value(double))

  implicit val characterMetaInstance: Meta[Char] =
    Meta.basic[Char]((record, idx) => record.get(idx).asInt().toChar, char => Values.value(char.toInt))

  implicit val stringMetaInstance: Meta[String] =
    Meta.basic[String]((record, idx) => record.get(idx).asString(), string => Values.value(string))

  implicit val booleanMetaInstance: Meta[Boolean] =
    Meta.basic[Boolean]((record, idx) => record.get(idx).asBoolean(), bool => Values.value(bool))

  implicit val offsetTimeMetaInstance: Meta[OffsetTime] =
    Meta.basic[OffsetTime]((record, idx) => record.get(idx).asOffsetTime(), offsetTime => Values.value(offsetTime))

  implicit val localDateMetaInstance: Meta[LocalDate] =
    Meta.basic[LocalDate]((record, idx) => record.get(idx).asLocalDate(), localDate => Values.value(localDate))

  implicit val localDateTimeMetaInstance: Meta[LocalDateTime] =
    Meta.basic[LocalDateTime]((record, idx) => record.get(idx).asLocalDateTime(), localDateTime => Values.value(localDateTime))

  implicit val localTimeMetaInstance: Meta[LocalTime] =
    Meta.basic[LocalTime]((record, idx) => record.get(idx).asLocalTime(), localTime => Values.value(localTime))

  implicit val offsetDateTimeMetaInstance: Meta[OffsetDateTime] =
    Meta.basic[OffsetDateTime]((record, idx) => record.get(idx).asOffsetDateTime(), offsetDateTime => Values.value(offsetDateTime))

  implicit val zonedDateTimeMetaInstance: Meta[ZonedDateTime] =
    Meta.basic[ZonedDateTime]((record, idx) => record.get(idx).asZonedDateTime(), zonedDateTime => Values.value(zonedDateTime))

  implicit val byteArrayMetaInstance: Meta[Array[Byte]] =
    Meta.basic[Array[Byte]]((record, idx) => record.get(idx).asByteArray(), byteArray => Values.value(byteArray))

  implicit def listMetaInstance[A: Get: Put: ClassTag]: Meta[List[A]] =
    Meta.basic[List[A]]((record, idx) => record.get(idx).asL(), list => Values.value(list.map(Put[A].put): _*))

  implicit def arrayMetaInstance[A: Get: Put: ClassTag]: Meta[Array[A]] = Meta[List[A]].imap(_.toArray)(_.toList)

  implicit def mapMetaInstance[A: Get: Put: ClassTag]: Meta[Map[String, A]] =
    Meta.basic[Map[String, A]](
      value => value.asMap[A](Get[A].get(_)).asScala.toMap,
      map => {
        val mapOfValues: Map[String, Value] = map.view.mapValues(Put[A].put).toMap
        val javaMap: util.Map[String, Value] = mapOfValues.asJava
        Values.value(javaMap)
      }
    )
}
