package neo4s.utils

import cats.syntax.option._
import neo4s.BaseSpec
import org.neo4j.driver.Values

class MetaSpec extends BaseSpec with MetaInstances {
  "Meta" should {
    "put and get Byte" in {
      val meta = Meta[Byte]

      val value: Byte = 1
      val to = meta.put.unsafeToValueNonNullable(value)
      val from = meta.get.unsafeGetNonNullable(to)

      val toOption = meta.put.unsafeToValueNullable(value.some)
      val fromOption = meta.get.unsafeGetNullable(toOption)

      val toOptionNone = meta.put.unsafeToValueNullable(None)
      val fromOptionNone = meta.get.unsafeGetNullable(toOptionNone)

      assertResult(Values.value(value.toInt))(to)
      assertResult(value)(from)

      assertResult(Values.value(value.toInt))(toOption)
      assertResult(value.some)(fromOption)

      assertResult(Values.NULL)(toOptionNone)
      assertResult(None)(fromOptionNone)
    }

    "put and get Short" in {}

    "put and get Int" in {}

    "put and get Long" in {}

    "put and get Float" in {}

    "put and get Double" in {}

    "put and get Char" in {}

    "put and get String" in {}

    "put and get Boolean" in {}

    "put and get OffsetTime" in {}

    "put and get LocalDate" in {}

    "put and get LocalDateTime" in {}

    "put and get LocalTime" in {}

    "put and get OffsetDateTime" in {}

    "put and get ZonedDateTime" in {}

    "put and get Array[Byte]" in {}
  }
}
