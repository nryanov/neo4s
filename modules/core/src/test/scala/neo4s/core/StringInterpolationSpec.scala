package neo4s.core

import neo4s.BaseSpec
import neo4s._
import neo4s.implicits._

class StringInterpolationSpec extends BaseSpec {
  "cypher string interpolation" should {
    "return cypher query" in {
      val name = "Name"
      val query = cypher"create (n:Person {name: $name})"

      assertResult(CypherQuery("create (n:Person {name: $arg0})", List("arg0"), List(Element.Arg(name, Put[String]))))(query)
    }
  }
}
