package neo4s

import org.neo4j.driver.{Query, Value, Values}

final case class CypherQuery(private val queryText: String, private val names: List[String], private val elements: List[Element]) {
  private implicit lazy val namedElementWrite: Write[elements.type] = {
    val toValues: elements.type => List[Value] = elements =>
      elements.map(value =>
        value match {
          case Element.Arg(a, put) => put.unsafeToValueNonNullable(a)
          case Element.Opt(a, put) => put.unsafeToValueNullable(a)
        }
      )

    val unsafeSet: elements.type => List[Value] = elements => toValues(elements)

    new Write[elements.type](unsafeSet)
  }

  def query[A: Read]: PreparedQuery[A] = {
    val query: Query = new Query(queryText, namedElementWrite.toValue(elements, names))
    PreparedQuery[A](query)
  }

  def update: PreparedAction = {
    val query: Query = new Query(queryText, namedElementWrite.toValue(elements, names))
    PreparedAction(query)
  }
}