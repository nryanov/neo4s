package neo4s.core

import java.time.LocalDate

import neo4s.BaseSpec
import neo4s._
import neo4s.implicits._
import org.neo4j.driver.internal.InternalRecord
import org.neo4j.driver.{Record, Value}

import scala.jdk.CollectionConverters._

class ReadWriteSpec extends BaseSpec {
  import ReadWriteSpec._

  "Read & Write" should {
    "correctly write and read value" in {
      val a = A(1, "2", Some(3), List(LocalDate.of(2000, 1, 1)))
      val query = cypher"create (a:A {f1: ${a.f1}, f2: ${a.f2}, f3: ${a.f3}, f4: ${a.f4})"

      val value: Value = query.namedElementWrite.toValue(query.elements, List("arg0", "arg1", "arg2", "arg3"))
      val read = Read[A]

      // it may be not consistent all the time (?).
      // Here it is needed to reverse array of values because otherwise they appear like this ([2000-01-01], 3, "2", 1).
      // In the meantime the Read[A] implementation expects to see concrete values in specific order: (Int, String, Option[Long], List[LocalDate])
      val record: Record = new InternalRecord(List("arg0", "arg1", "arg2", "arg3").asJava, value.values().asScala.toSeq.reverse.toArray)

      val expectedValue = Map("arg0" -> 1, "arg1" -> "2", "arg2" -> 3L, "arg3" -> List(LocalDate.of(2000, 1, 1)).asJava)

      assertResult(expectedValue)(value.asMap().asScala.toMap)
      assertResult(a)(read.unsafeGet(record, 0))
    }
  }
}

object ReadWriteSpec {
  case class A(f1: Int, f2: String, f3: Option[Long], f4: List[LocalDate])
}
