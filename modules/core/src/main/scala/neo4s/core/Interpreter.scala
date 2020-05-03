package neo4s.core

import cats.data.Kleisli
import cats.effect.Sync
import cats.syntax.functor._
import cats.~>
import neo4s.core.ExecutableOp.{DelayR, ExecutableIO}
import org.neo4j.driver.Transaction

object Interpreter {
  type RunnableF[F[_], A] = Kleisli[F, Transaction, A]

  def compile[F[_]: Sync, A](executableIO: ExecutableIO[A]): RunnableF[F, A] = inject(executableIO)

  private def inject[F[_]: Sync, A](executableIO: ExecutableIO[A]): Kleisli[F, Transaction, A] =
    Kleisli[F, Transaction, A](runner => executableIO.foldMap(interpreter(runner)))

  private def interpreter[F[_]: Sync](transaction: Transaction): ExecutableOp ~> F = new (ExecutableOp ~> F) {
    override def apply[A](fa: ExecutableOp[A]): F[A] = fa match {
      case DelayR(query, action) => Sync[F].delay(transaction.run(query)).map(action)
    }
  }
}
