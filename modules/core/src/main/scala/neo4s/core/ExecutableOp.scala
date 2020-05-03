package neo4s.core

import cats.free.Free
import org.neo4j.driver.{Query, Result}

sealed trait ExecutableOp[A]

object ExecutableOp {
  type ExecutableIO[A] = Free[ExecutableOp, A]

  // smart constructors
  val unit: ExecutableIO[Unit] = Free.pure[ExecutableOp, Unit](())

  def pure[A](a: A): ExecutableIO[A] = Free.pure[ExecutableOp, A](a)

  def delayR[A](query: Query, action: Result => A): ExecutableIO[A] = Free.liftF(DelayR(query, action))

  // Commands
  final case class DelayR[A](query: Query, action: Result => A) extends ExecutableOp[A]
}
