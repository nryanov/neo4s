package neo4s

import neo4s.ExecutableOp.ExecutableIO
import org.neo4j.driver.summary.SummaryCounters
import org.neo4j.driver.{Query, Result}

final class PreparedAction(query: Query) {
  def run: ExecutableIO[SummaryCounters] = {
    val action: Result => SummaryCounters = result => result.consume().counters()

    ExecutableOp.delayR(query, action)
  }
}

object PreparedAction {
  def apply(query: Query): PreparedAction = new PreparedAction(query)
}
