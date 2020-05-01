package neo4s

import cats.effect.{Async, ContextShift, ExitCase}
import cats.free.Free
import cats.~>

import scala.concurrent.ExecutionContext

sealed trait ExecutableOp[A] {
  def visit[F[_]](visitor: ExecutableOp.Visitor[F]): F[A]
}

object ExecutableOp {
  type ExecutableIO[A] = Free[ExecutableOp, A]

  trait Visitor[F[_]] extends (ExecutableOp ~> F) {
    final def apply[A](fa: ExecutableOp[A]): F[A] = fa.visit(this)

    def delay[A](a: () => A): F[A]

    def handleErrorWith[A](fa: ExecutableIO[A], f: Throwable => ExecutableIO[A]): F[A]

    def raiseError[A](e: Throwable): F[A]

    def async[A](k: (Either[Throwable, A] => Unit) => Unit): F[A]

    def asyncF[A](k: (Either[Throwable, A] => Unit) => ExecutableIO[Unit]): F[A]

    def bracketCase[A, B](acquire: ExecutableIO[A])(use: A => ExecutableIO[B])(
      release: (A, ExitCase[Throwable]) => ExecutableIO[Unit]
    ): F[B]

    def shift: F[Unit]

    def evalOn[A](ec: ExecutionContext)(fa: ExecutableIO[A]): F[A]
  }

  val unit: ExecutableIO[Unit] = Free.pure[ExecutableOp, Unit](())

  def pure[A](a: A): ExecutableIO[A] = Free.pure[ExecutableOp, A](a)

  def delay[A](a: => A): ExecutableIO[A] = Free.liftF(Delay(() => a))

  def handleErrorWith[A](fa: ExecutableIO[A], f: Throwable => ExecutableIO[A]): ExecutableIO[A] =
    Free.liftF[ExecutableOp, A](HandleErrorWith(fa, f))

  def raiseError[A](err: Throwable): ExecutableIO[A] = Free.liftF[ExecutableOp, A](RaiseError(err))

  def async[A](k: (Either[Throwable, A] => Unit) => Unit): ExecutableIO[A] = Free.liftF[ExecutableOp, A](Async1(k))

  def asyncF[A](k: (Either[Throwable, A] => Unit) => ExecutableIO[Unit]): ExecutableIO[A] = Free.liftF[ExecutableOp, A](AsyncF(k))

  def bracketCase[A, B](acquire: ExecutableIO[A])(use: A => ExecutableIO[B])(
    release: (A, ExitCase[Throwable]) => ExecutableIO[Unit]
  ): ExecutableIO[B] = Free.liftF[ExecutableOp, B](BracketCase(acquire, use, release))

  val shift: ExecutableIO[Unit] = Free.liftF[ExecutableOp, Unit](Shift)

  def evalOn[A](ec: ExecutionContext)(fa: ExecutableIO[A]): Free[ExecutableOp, A] = Free.liftF[ExecutableOp, A](EvalOn(ec, fa))

  final case class Delay[A](a: () => A) extends ExecutableOp[A] {
    def visit[F[_]](v: Visitor[F]): F[A] = v.delay(a)
  }

  final case class HandleErrorWith[A](fa: ExecutableIO[A], f: Throwable => ExecutableIO[A]) extends ExecutableOp[A] {
    def visit[F[_]](v: Visitor[F]): F[A] = v.handleErrorWith(fa, f)
  }

  final case class RaiseError[A](e: Throwable) extends ExecutableOp[A] {
    def visit[F[_]](v: Visitor[F]): F[A] = v.raiseError(e)
  }

  final case class Async1[A](k: (Either[Throwable, A] => Unit) => Unit) extends ExecutableOp[A] {
    def visit[F[_]](v: Visitor[F]): F[A] = v.async(k)
  }

  final case class AsyncF[A](k: (Either[Throwable, A] => Unit) => ExecutableIO[Unit]) extends ExecutableOp[A] {
    def visit[F[_]](v: Visitor[F]): F[A] = v.asyncF(k)
  }

  final case class BracketCase[A, B](
    acquire: ExecutableIO[A],
    use: A => ExecutableIO[B],
    release: (A, ExitCase[Throwable]) => ExecutableIO[Unit]
  ) extends ExecutableOp[B] {
    def visit[F[_]](v: Visitor[F]): F[B] = v.bracketCase(acquire)(use)(release)
  }
  final case object Shift extends ExecutableOp[Unit] {
    def visit[F[_]](v: Visitor[F]): F[Unit] = v.shift
  }
  final case class EvalOn[A](ec: ExecutionContext, fa: ExecutableIO[A]) extends ExecutableOp[A] {
    def visit[F[_]](v: Visitor[F]): F[A] = v.evalOn(ec)(fa)
  }

  implicit val asyncExecutableIO: Async[ExecutableIO] = new Async[ExecutableIO] {
    val asyncM = Free.catsFreeMonadForFree[ExecutableOp]

    override def async[A](k: (Either[Throwable, A] => Unit) => Unit): ExecutableIO[A] = ExecutableOp.async(k)

    override def asyncF[A](k: (Either[Throwable, A] => Unit) => ExecutableIO[Unit]): ExecutableIO[A] = ExecutableOp.asyncF(k)

    override def suspend[A](thunk: => ExecutableIO[A]): ExecutableIO[A] = asyncM.flatten(ExecutableOp.delay(thunk))

    override def bracketCase[A, B](acquire: ExecutableIO[A])(use: A => ExecutableIO[B])(
      release: (A, ExitCase[Throwable]) => ExecutableIO[Unit]
    ): ExecutableIO[B] = ExecutableOp.bracketCase(acquire)(use)(release)

    override def flatMap[A, B](fa: ExecutableIO[A])(f: A => ExecutableIO[B]): ExecutableIO[B] = asyncM.flatMap(fa)(f)

    override def tailRecM[A, B](a: A)(f: A => ExecutableIO[Either[A, B]]): ExecutableIO[B] = asyncM.tailRecM(a)(f)

    override def raiseError[A](e: Throwable): ExecutableIO[A] = ExecutableOp.raiseError(e)

    override def handleErrorWith[A](fa: ExecutableIO[A])(f: Throwable => ExecutableIO[A]): ExecutableIO[A] =
      ExecutableOp.handleErrorWith(fa, f)

    override def pure[A](x: A): ExecutableIO[A] = asyncM.pure(x)
  }

  implicit val contextShiftExecutableIO: ContextShift[ExecutableIO] = new ContextShift[ExecutableIO] {
    override def shift: ExecutableIO[Unit] = ExecutableOp.shift

    override def evalOn[A](ec: ExecutionContext)(fa: ExecutableIO[A]): ExecutableIO[A] = ExecutableOp.evalOn(ec)(fa)
  }
}
