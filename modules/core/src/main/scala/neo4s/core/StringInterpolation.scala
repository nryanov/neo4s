package neo4s.core

object StringInterpolation {
  final class CypherInterpolation(val sc: StringContext) extends AnyVal {
    def cypher(args: Element*): CypherQuery =
      CypherQuery(sc.parts.toList, args.toList)
  }

}

trait StringInterpolation {
  import StringInterpolation._

  implicit def toCypherInterpolator(sc: StringContext): CypherInterpolation = new CypherInterpolation(sc)
}
