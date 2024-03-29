package neo4s.core

import java.net.URI

import cats.effect.kernel.Outcome
import cats.effect.kernel.Resource.ExitCase
import cats.effect.{MonadCancel, Resource, Sync}
import neo4s.core.ExecutableOp.ExecutableIO
import org.neo4j.driver._

final class Neo4jTransactor[F[_]](driver: Driver)(implicit F: Sync[F]) {

  def transact[A](executableIO: ExecutableIO[A]): F[A] = session().use { session =>
    MonadCancel[F, Throwable].bracketCase[Transaction, A](F.delay(session.beginTransaction())) { tx =>
      Interpreter.compile(executableIO).run(tx)
    } { case (tx, code) =>
      code match {
        case Outcome.Succeeded(fa) => F.whenA(tx.isOpen)(F.delay(tx.commit()))
        case Outcome.Errored(e)    => F.flatMap(F.whenA(tx.isOpen)(F.delay(tx.rollback())))(_ => F.raiseError(e))
        case Outcome.Canceled()    => F.whenA(tx.isOpen)(F.delay(tx.rollback()))
      }
    }
  }

  private def session(): Resource[F, Session] = {
    def acquire: F[Session] = F.delay(driver.session())

    def release(session: Session): F[Unit] = F.delay(session.close())

    Resource.make(acquire)(release)
  }
}

object Neo4jTransactor {
  def create[F[_]: Sync](uri: String): Resource[F, Neo4jTransactor[F]] = create(uri, Config.builder().withLogging(Logging.slf4j()).build())

  def create[F[_]: Sync](uri: URI): Resource[F, Neo4jTransactor[F]] = create(uri, Config.builder().withLogging(Logging.slf4j()).build())

  def create[F[_]: Sync](uri: String, config: Config): Resource[F, Neo4jTransactor[F]] = create(uri, config, AuthTokens.none())

  def create[F[_]: Sync](uri: URI, config: Config): Resource[F, Neo4jTransactor[F]] = create(uri, config, AuthTokens.none())

  def create[F[_]: Sync](uri: String, config: Config, authToken: AuthToken): Resource[F, Neo4jTransactor[F]] =
    create(new URI(uri), config, authToken)

  def create[F[_]: Sync](uri: URI, config: Config, authToken: AuthToken): Resource[F, Neo4jTransactor[F]] = create0(uri, config, authToken)

  private def create0[F[_]: Sync](
    uri: URI,
    config: Config = Config.builder().withLogging(Logging.slf4j()).build(),
    authToken: AuthToken = AuthTokens.none()
  ): Resource[F, Neo4jTransactor[F]] = {
    def acquire: F[Driver] = Sync[F].delay(GraphDatabase.driver(uri, authToken, config))

    def release(driver: Driver): F[Unit] = Sync[F].delay(driver.close())

    Resource.make(acquire)(release).map(new Neo4jTransactor[F](_))
  }
}
