package neo4s.core

import cats.data.Kleisli
import cats.effect.Sync
import cats.syntax.functor._
import cats.~>
import neo4s.core.ExecutableOp.ExecutableIO
import org.neo4j.driver.Transaction

object Interpreter {
  type RunnableF[F[_], A] = Kleisli[F, Transaction, A]

  def compile[F[_]: Sync, A](executableIO: ExecutableIO[A]): RunnableF[F, A] = inject(executableIO)

  private def inject[F[_]: Sync, A](executableIO: ExecutableIO[A]): Kleisli[F, Transaction, A] =
    Kleisli[F, Transaction, A](runner => executableIO.foldMap(interpreter(runner)))

  private def interpreter[F[_]](transaction: Transaction)(implicit F: Sync[F]): ExecutableOp ~> F = new (ExecutableOp ~> F) {
    override def apply[A](fa: ExecutableOp[A]): F[A] = fa match {
      case ExecutableOp.DelayR(query, action)  => F.delay(transaction.run(query)).map(action)
      case ExecutableOp.Delay(thunk)           => F.delay(thunk())
      case ExecutableOp.RaiseError(e)          => F.raiseError(e)
      case ExecutableOp.HandleErrorWith(fa, f) => F.handleErrorWith(compile(fa).run(transaction))(e => compile(f(e)).run(transaction))
      case ExecutableOp.HandleError(fa, f)     => F.handleError(compile(fa).run(transaction))(e => f(e))
      case ExecutableOp.Raw(fa)                => F.delay(fa(transaction))
      case ExecutableOp.Commit                 => F.whenA(transaction.isOpen)(F.delay(transaction.commit()))
      case ExecutableOp.Rollback               => F.whenA(transaction.isOpen)(F.delay(transaction.rollback()))
    }
  }
}
