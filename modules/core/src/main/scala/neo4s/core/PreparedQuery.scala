package neo4s.core

import cats.data.NonEmptyList
import neo4s.core.ExecutableOp.ExecutableIO
import neo4s.utils.Read
import org.neo4j.driver.{Query, Result}

import neo4s.utils.CollectionCompat._

final class PreparedQuery[A](query: Query)(implicit read: Read[A]) {
  private val START_INDEX: Int = 0

  def option: ExecutableIO[Option[A]] = {
    val action: Result => Option[A] = result => {
      if (result.hasNext) {
        Some(read.unsafeGet(result.next(), START_INDEX))
      } else {
        None
      }
    }

    ExecutableOp.delayR(query, action)
  }

  def list: ExecutableIO[List[A]] = {
    val action: Result => List[A] = result => {
      result.list().map(record => read.unsafeGet(record, START_INDEX))
    }

    ExecutableOp.delayR(query, action)
  }

  def unique: ExecutableIO[A] = {
    val action: Result => A = result => read.unsafeGet(result.single(), START_INDEX)

    ExecutableOp.delayR(query, action)
  }

  def nel: ExecutableIO[NonEmptyList[A]] = {
    val action: Result => NonEmptyList[A] = result => NonEmptyList.fromList(result.list().map(read.unsafeGet(_, START_INDEX))).get

    ExecutableOp.delayR(query, action)
  }
}

object PreparedQuery {
  def apply[A: Read](query: Query): PreparedQuery[A] = new PreparedQuery(query: Query)
}
