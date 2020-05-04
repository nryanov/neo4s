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

  def delay[A](thunk: () => A): ExecutableIO[A] = Free.liftF(Delay(thunk))

  def raiseError[A](e: Throwable): ExecutableIO[Unit] = Free.liftF(RaiseError(e))

  def handleErrorWith[A](fa: ExecutableIO[A], f: Throwable => ExecutableIO[A]): ExecutableIO[A] = Free.liftF(HandleErrorWith(fa, f))

  // Commands
  final case class DelayR[A](query: Query, action: Result => A) extends ExecutableOp[A]

  final case class Delay[A](thunk: () => A) extends ExecutableOp[A]

  final case class RaiseError[A](e: Throwable) extends ExecutableOp[A]

  final case class HandleErrorWith[A](fa: ExecutableIO[A], f: Throwable => ExecutableIO[A]) extends ExecutableOp[A]

}
