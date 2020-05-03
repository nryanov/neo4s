package neo4s.utils

import neo4s.BaseSpec
import neo4s.utils.Write._

object WriteSpec {
  final case class A(a: Int)

  final case class B(a: String, b: Long)

  final case class C(a: String, b: Long, c: Option[Int])
}

class WriteSpec extends BaseSpec {
  import WriteSpec._

  "write" should {
    "return instances for primitives" in {
      Write[Int]
      Write[String]

      assert(true)
    }

    "derive instances" in {
      Write[A]
      Write[B]
      Write[C]

      assert(true)
    }
  }
}
