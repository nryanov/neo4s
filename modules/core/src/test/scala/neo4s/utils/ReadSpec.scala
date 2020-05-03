package neo4s.utils

import neo4s.BaseSpec
import neo4s.utils.Read._

object ReadSpec {
  final case class A(a: Int)

  final case class B(a: String, b: Long)

  final case class C(a: String, b: Long, c: Option[Int])
}

class ReadSpec extends BaseSpec {
  import ReadSpec._

  "read" should {
    "exists for primitive types" in {
      Read[Int]
      Read[String]

      assert(true)
    }

    "be derived for case classes" in {
      Read[A]
      Read[B]
      Read[C]

      assert(true)
    }
  }
}
