package neo4s.utils

import neo4s.BaseSpec
import neo4s.utils.Get._
import shapeless.test._

object GetSpec {
  case class Foo(a: Int) extends AnyVal
  case class Boo(b: Long) extends AnyVal

  case class Incorrect(a: Int, b: Long, c: String)
}

class GetSpec extends BaseSpec {
  import GetSpec._

  "get" should {
    "return instances for primitives" in {
      Get[Int]
      Get[String]

      assert(true)
    }

    "derive instance" in {
      Get[Foo]
      Get[Boo]

      assert(true)
    }

    "not compile" in {
      illTyped("Get[Incorrect]")
      illTyped("Get[(Int, Long, Option[String])]")

      assert(true)
    }
  }
}
