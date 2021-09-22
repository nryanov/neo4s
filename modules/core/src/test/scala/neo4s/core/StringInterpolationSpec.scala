package neo4s.core

import neo4s.BaseSpec
import neo4s._
import neo4s.implicits._

class StringInterpolationSpec extends BaseSpec {
  "cypher string interpolation" should {
    "return cypher query" in {
      val name = "Name"
      val query = cypher"create (n:Person {name: $name})"

      assertResult(("create (n:Person {name: $arg0})", List("arg0"), List(Element.Arg(name, Put[String]))))(
        (query.queryText, query.names, query.args)
      )
    }

    "concatenate cypher queries" in {
      val name: String = "Name"
      val limit: Int = 1

      val query1 = cypher"MATCH (n:Person {name: $name})"
      val query2 = cypher"RETURN n"
      val query3 = cypher"LIMIT $limit"

      val query = query1 ++ query2 ++ query3

      assertResult(
        (
          "MATCH (n:Person {name: $arg0}) RETURN n LIMIT $arg1",
          List("arg0", "arg1"),
          List(Element.Arg(name, Put[String]), Element.Arg(limit, Put[Int]))
        )
      )(
        (query.queryText, query.names, query.args)
      )
    }
  }
}
