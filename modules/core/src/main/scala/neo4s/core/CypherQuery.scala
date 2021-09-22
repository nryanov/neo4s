package neo4s.core

import neo4s.utils.{Read, Write}
import org.neo4j.driver.{Query, Value}
import scala.collection.mutable

final case class CypherQuery(
  private[core] val parts: List[String],
  private[core] val args: List[Element]
) {
  def query[A: Read]: PreparedQuery[A] = {
    val query: Query = new Query(queryText, namedArgsWrite.toValue(args, names))
    PreparedQuery[A](query)
  }

  def update: PreparedAction = {
    val query: Query = new Query(queryText, namedArgsWrite.toValue(args, names))
    PreparedAction(query)
  }

  private[core] lazy val namedArgsWrite: Write[args.type] = {
    val toValues: args.type => List[Value] = args =>
      args.map {
        case Element.Arg(a, put) => put.unsafeToValueNonNullable(a)
        case Element.Opt(a, put) => put.unsafeToValueNullable(a)
      }

    val unsafeSet: args.type => List[Value] = args => toValues(args)

    new Write[args.type](unsafeSet)
  }

  private[core] lazy val (queryText: String, names: List[String]) = {
    val partsIterator = parts.iterator
    val names = new mutable.ListBuffer[String]

    var argNumber = 0
    val query = new StringBuilder(partsIterator.next())

    while (partsIterator.hasNext) {
      val argName = s"arg$argNumber"
      val argNameReplacement = s"$$$argName"
      query.append(argNameReplacement)
      query.append(partsIterator.next())
      argNumber += 1

      names.append(argName)
    }

    (query.toString(), names.toList)
  }
}

object CypherQuery {
  implicit class RichCypherQuery(val current: CypherQuery) extends AnyVal {
    def ++(another: CypherQuery): CypherQuery = {
      val reversedQuery = current.parts.reverse
      val (headC, tailC) = (reversedQuery.head, reversedQuery.tail)
      val (headA, tailA) = (another.parts.head, another.parts.tail)

      val queryParts: List[String] = (s"$headC $headA" :: tailC).reverse ::: tailA

      CypherQuery(queryParts, current.args ++ another.args)
    }
  }
}
