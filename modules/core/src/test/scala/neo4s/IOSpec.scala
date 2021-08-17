package neo4s

import java.util.concurrent.Executors

import cats.effect.IO
import org.scalatest.Assertion

import scala.concurrent.{ExecutionContext, ExecutionContextExecutor}
import cats.effect.Temporal

trait IOSpec extends BaseSpec {
  type F[A] = IO[A]

  implicit val ex: ExecutionContextExecutor = ExecutionContext.fromExecutor(Executors.newSingleThreadExecutor())
  implicit val cs: ContextShift[IO] = IO.contextShift(ex)
  implicit val timer: Temporal[IO] = IO.timer(ex)

  def runF(body: F[Assertion]): Assertion = body.unsafeRunSync()

}
