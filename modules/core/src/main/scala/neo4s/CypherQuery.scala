package neo4s

import org.neo4j.driver.Value

final case class CypherQuery(query: String, names: List[String], elements: List[Element]) {
  implicit lazy val namedElementWrite: Write[elements.type] = {
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

}
