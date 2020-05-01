import scala.collection.mutable

package object neo4s {
  implicit class CypherInterpolation(val sc: StringContext) extends AnyVal {
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

        elements.addOne(argsIterator.next())
        names.addOne(argName)
      }

      CypherQuery(query.toString(), names.toList, elements.toList)
    }
  }
}
