package neo4s.utils

import neo4s.BaseSpec
import neo4s.utils.Meta._
import neo4s.utils.Put._
import shapeless.test._

object PutSpec {
  case class Foo(a: Int) extends AnyVal
  case class Boo(b: Long) extends AnyVal

  case class Incorrect(a: Int, b: Long, c: String)
}

class PutSpec extends BaseSpec {
  import PutSpec._

  "put" should {
    "return instances for primitives" in {
      Put[Int]
      Put[String]

      assert(true)
    }

    "derive instances" in {
      Put[Foo]
      Put[Boo]

      assert(true)
    }

    "not compile" in {
      illTyped("Put[Incorrect]")
      illTyped("Put[(Int, Long, Option[String])]")

      assert(true)
    }
  }
}
