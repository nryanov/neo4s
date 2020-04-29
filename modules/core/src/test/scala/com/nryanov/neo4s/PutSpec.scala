package com.nryanov.neo4s

import Put._
import Meta._

object PutSpec {
  case class Foo(a: Int) extends AnyVal
  case class Boo(b: Long) extends AnyVal
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
  }
}
