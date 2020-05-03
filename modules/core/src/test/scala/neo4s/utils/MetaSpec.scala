package neo4s.utils

import java.time.{LocalDate, LocalDateTime, LocalTime, OffsetDateTime, OffsetTime, ZoneId, ZoneOffset, ZonedDateTime}
import java.util.UUID

import cats.syntax.option._
import neo4s.BaseSpec
import org.neo4j.driver.{Value, Values}

import scala.jdk.CollectionConverters._

class MetaSpec extends BaseSpec with MetaInstances {
  def assertions[A](
    meta: Meta[A],
    value: A,
    valueConverter: A => Any = { a: A =>
      a
    }
  ): Unit = {
    val to: Value = meta.put.unsafeToValueNonNullable(value)
    val from: A = meta.get.unsafeGetNonNullable(to)

    val toOption: Value = meta.put.unsafeToValueNullable(value.some)
    val fromOption: Option[A] = meta.get.unsafeGetNullable(toOption)

    val toOptionNone: Value = meta.put.unsafeToValueNullable(None)
    val fromOptionNone: Option[A] = meta.get.unsafeGetNullable(toOptionNone)

    assertResult(Values.value(valueConverter(value)))(to)
    assertResult(value)(from)

    assertResult(Values.value(valueConverter(value)))(toOption)
    assertResult(value.some)(fromOption)

    assertResult(Values.NULL)(toOptionNone)
    assertResult(None)(fromOptionNone)

    assertThrows[Throwable](meta.put.unsafeToValueNonNullable(null.asInstanceOf[A]))
    assertThrows[Throwable](meta.get.unsafeGetNonNullable(Values.NULL))
  }

  "Meta" should {
    "put and get Byte" in {
      val meta = Meta[Byte]

      val value: Byte = 1

      assertions(meta, value)
    }

    "put and get Short" in {
      val meta = Meta[Short]

      val value: Short = 1

      assertions(meta, value)
    }

    "put and get Int" in {
      val meta = Meta[Int]

      val value: Int = 1

      assertions(meta, value)
    }

    "put and get Long" in {
      val meta = Meta[Long]

      val value: Long = 1L

      assertions(meta, value)
    }

    "put and get Float" in {
      val meta = Meta[Float]

      val value: Float = 1.0f

      assertions(meta, value)
    }

    "put and get Double" in {
      val meta = Meta[Double]

      val value: Double = 1.0d

      assertions(meta, value)
    }

    "put and get Char" in {
      val meta = Meta[Char]

      val value: Char = 'a'

      assertions[Char](meta, value, v => v.toInt)
    }

    "put and get String" in {
      val meta = Meta[String]

      val value: String = "a"

      assertions(meta, value)
    }

    "put and get Boolean" in {
      val meta = Meta[Boolean]

      val value: Boolean = true

      assertions(meta, value)
    }

    "put and get UUID" in {
      val meta = Meta[UUID]

      val value: UUID = UUID.fromString("123e4567-e89b-12d3-a456-426655440000")

      assertions[UUID](meta, value, uuid => uuid.toString)
    }

    "put and get BigInt" in {
      val meta = Meta[BigInt]

      val value: BigInt = BigInt(1)

      assertions[BigInt](meta, value, v => v.toString())
    }

    "put and get BigDecimal" in {
      val meta = Meta[BigDecimal]

      val value: BigDecimal = BigDecimal(1.0)

      assertions[BigDecimal](meta, value, v => v.toString())
    }

    "put and get OffsetTime" in {
      val meta = Meta[OffsetTime]

      val value: OffsetTime = OffsetTime.of(1, 1, 1, 1, ZoneOffset.UTC)

      assertions(meta, value)
    }

    "put and get LocalDate" in {
      val meta = Meta[LocalDate]

      val value: LocalDate = LocalDate.of(2000, 1, 1)

      assertions(meta, value)
    }

    "put and get LocalDateTime" in {
      val meta = Meta[LocalDateTime]

      val value: LocalDateTime = LocalDateTime.of(2000, 1, 1, 1, 1, 1)

      assertions(meta, value)
    }

    "put and get LocalTime" in {
      val meta = Meta[LocalTime]

      val value: LocalTime = LocalTime.of(1, 1)

      assertions(meta, value)
    }

    "put and get OffsetDateTime" in {
      val meta = Meta[OffsetDateTime]

      val value: OffsetDateTime = OffsetDateTime.of(2000, 1, 1, 1, 1, 1, 1, ZoneOffset.UTC)

      assertions(meta, value)
    }

    "put and get ZonedDateTime" in {
      val meta = Meta[ZonedDateTime]

      val value: ZonedDateTime = ZonedDateTime.of(2000, 1, 1, 1, 1, 1, 1, ZoneId.systemDefault())

      assertions(meta, value)
    }

    "put and get Array[Byte]" in {
      val meta = Meta[Array[Byte]]

      val value: Array[Byte] = Array[Byte](1, 2, 3, 4, 5)

      assertions(meta, value)
    }

    "put and get Array[T]" in {
      val meta = Meta[Array[Int]]

      val value: Array[Int] = Array[Int](1, 2, 3, 4, 5)

      val to = meta.put.unsafeToValueNonNullable(value)
      val from = meta.get.unsafeGetNonNullable(to)

      val toOption = meta.put.unsafeToValueNullable(value.some)
      val fromOption = meta.get.unsafeGetNullable(toOption)

      val toOptionNone = meta.put.unsafeToValueNullable(None)
      val fromOptionNone = meta.get.unsafeGetNullable(toOptionNone)

      assertResult(Values.value(value))(to)
      assertResult(value)(from)

      assertResult(Values.value(value))(toOption)
      assertResult(value.toSeq.some)(fromOption.map(_.toSeq))

      assertResult(Values.NULL)(toOptionNone)
      assertResult(None)(fromOptionNone)

      assertThrows[Throwable](meta.put.unsafeToValueNonNullable(null.asInstanceOf[Array[Int]]))
      assertThrows[Throwable](meta.get.unsafeGetNonNullable(Values.NULL))
    }

    "put and get List[T]" in {
      val meta = Meta[List[Int]]

      val value: List[Int] = List[Int](1, 2, 3, 4, 5)

      val to = meta.put.unsafeToValueNonNullable(value)
      val from = meta.get.unsafeGetNonNullable(to)

      val toOption = meta.put.unsafeToValueNullable(value.some)
      val fromOption = meta.get.unsafeGetNullable(toOption)

      val toOptionNone = meta.put.unsafeToValueNullable(None)
      val fromOptionNone = meta.get.unsafeGetNullable(toOptionNone)

      assertResult(Values.value(value.asJavaCollection))(to)
      assertResult(value)(from)

      assertResult(Values.value(value.asJavaCollection))(toOption)
      assertResult(value.some)(fromOption)

      assertResult(Values.NULL)(toOptionNone)
      assertResult(None)(fromOptionNone)

      assertThrows[Throwable](meta.put.unsafeToValueNonNullable(null.asInstanceOf[List[Int]]))
      assertThrows[Throwable](meta.get.unsafeGetNonNullable(Values.NULL))
    }

    "put and get Map[String, T]" in {
      val meta = Meta[Map[String, Int]]

      val value: Map[String, Int] = Map[String, Int]("1" -> 1, "2" -> 2, "3" -> 3)

      val to = meta.put.unsafeToValueNonNullable(value)
      val from = meta.get.unsafeGetNonNullable(to)

      val toOption = meta.put.unsafeToValueNullable(value.some)
      val fromOption = meta.get.unsafeGetNullable(toOption)

      val toOptionNone = meta.put.unsafeToValueNullable(None)
      val fromOptionNone = meta.get.unsafeGetNullable(toOptionNone)

      assertResult(Values.value(value.asJava))(to)
      assertResult(value)(from)

      assertResult(Values.value(value.asJava))(toOption)
      assertResult(value.some)(fromOption)

      assertResult(Values.NULL)(toOptionNone)
      assertResult(None)(fromOptionNone)

      assertThrows[Throwable](meta.put.unsafeToValueNonNullable(null.asInstanceOf[Map[String, Int]]))
      assertThrows[Throwable](meta.get.unsafeGetNonNullable(Values.NULL))
    }
  }
}
