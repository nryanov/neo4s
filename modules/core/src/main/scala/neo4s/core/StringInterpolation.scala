package neo4s.core

import scala.collection.mutable

object StringInterpolation {
  final class CypherInterpolation(val sc: StringContext) extends AnyVal {
    def cypher(args: Element*): CypherQuery = {
      val partsIterator = sc.parts.iterator
      val argsIterator = args.iterator
      val elements = new mutable.ListBuffer[Element]
      val names = new mutable.ListBuffer[String]

      var argNumber = 0
      val query = new StringBuilder(partsIterator.next())

      while (partsIterator.hasNext) {
        val argName = s"arg$argNumber"
        val argNameReplacement = s"$$$argName"
        query.append(argNameReplacement)
        query.append(partsIterator.next())
        argNumber += 1

        elements.append(argsIterator.next())
        names.append(argName)
      }

      CypherQuery(query.toString(), names.toList, elements.toList)
    }
  }

}

trait StringInterpolation {
  import StringInterpolation._

  implicit def toCypherInterpolator(sc: StringContext): CypherInterpolation = new CypherInterpolation(sc)
}
