package neo4s

import java.net.URI

import cats.effect.{Bracket, ExitCase, Resource, Sync}
import neo4s.ExecutableOp.ExecutableIO
import org.neo4j.driver.{AuthToken, AuthTokens, Config, Driver, GraphDatabase, Session}

final class Neo4jTransactor[F[_]](driver: Driver)(implicit F: Sync[F]) {

  def transact[A](executableIO: ExecutableIO[A]): F[A] = session().use { session =>
    Bracket[F, Throwable].bracketCase(F.delay(session.beginTransaction())) { tx =>
      Interpreter.compile(executableIO).run(tx)
    } {
      case (tx, code) =>
        code match {
          case ExitCase.Completed => F.delay(tx.commit())
          case ExitCase.Error(_)  => F.delay(tx.rollback())
          case ExitCase.Canceled  => F.delay(tx.rollback())
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
  def create[F[_]: Sync](uri: String): Resource[F, Neo4jTransactor[F]] = create(uri, Config.defaultConfig())

  def create[F[_]: Sync](uri: URI): Resource[F, Neo4jTransactor[F]] = create(uri, Config.defaultConfig())

  def create[F[_]: Sync](uri: String, config: Config): Resource[F, Neo4jTransactor[F]] = create(uri, config, AuthTokens.none())

  def create[F[_]: Sync](uri: URI, config: Config): Resource[F, Neo4jTransactor[F]] = create(uri, config, AuthTokens.none())

  def create[F[_]: Sync](uri: String, config: Config, authToken: AuthToken): Resource[F, Neo4jTransactor[F]] =
    create(new URI(uri), config, authToken)

  def create[F[_]: Sync](uri: URI, config: Config, authToken: AuthToken): Resource[F, Neo4jTransactor[F]] = create0(uri, config, authToken)

  private def create0[F[_]: Sync](
    uri: URI,
    config: Config = Config.defaultConfig(),
    authToken: AuthToken = AuthTokens.none()
  ): Resource[F, Neo4jTransactor[F]] = {
    def acquire: F[Driver] = Sync[F].delay(GraphDatabase.driver(uri, authToken, config))

    def release(driver: Driver): F[Unit] = Sync[F].delay(driver.close())

    Resource.make(acquire)(release).map(new Neo4jTransactor[F](_))
  }
}
