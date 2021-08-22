package neo4s

import cats.effect.IO
import cats.effect.unsafe.IORuntime
import org.scalatest.Assertion

trait IOSpec extends BaseSpec {
  type F[A] = IO[A]

  implicit val runtime: IORuntime = IORuntime.global

  def runF(body: F[Assertion]): Assertion = body.unsafeRunSync()

}
