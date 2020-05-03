package neo4s

import java.util.concurrent.Executors

import cats.effect.{ContextShift, IO, Timer}
import org.scalatest.Assertion

import scala.concurrent.{ExecutionContext, ExecutionContextExecutor}

trait IOSpec extends BaseSpec {
  type F[A] = IO[A]

  implicit val ex: ExecutionContextExecutor = ExecutionContext.fromExecutor(Executors.newSingleThreadExecutor())
  implicit val cs: ContextShift[IO] = IO.contextShift(ex)
  implicit val timer: Timer[IO] = IO.timer(ex)

  def runF(body: F[Assertion]): Assertion = body.unsafeRunSync()

}
