package neo4s

import java.net.URI
import java.util.concurrent.CompletionStage

import cats.effect.{Async, Bracket, ExitCase, Resource, Sync}
import org.neo4j.driver.async.AsyncSession
import org.neo4j.driver.{AuthToken, AuthTokens, Config, Driver, GraphDatabase, Session}

final class Neo4jTransactor[F[_]](driver: Driver)(implicit F: Async[F]) {

  def transact[A](executableIO: ExecutableIO[A]): F[A] = session().use { session =>
    Bracket[F, Throwable].bracketCase(F.delay(session.beginTransaction())) { tx =>
      ???
    } {
      case (tx, code) =>
        code match {
          case ExitCase.Completed => F.delay(tx.commit())
          case ExitCase.Error(_)  => F.delay(tx.rollback())
          case ExitCase.Canceled  => F.delay(tx.rollback())
        }
    }
  }

  def transactAsync[A](executableIO: ExecutableIO[A]): F[A] = asyncSession().use { session =>
    Bracket[F, Throwable].bracketCase(async0(session.beginTransactionAsync())) { tx =>
      ???
    } {
      case (tx, code) =>
        code match {
          case ExitCase.Completed => async0Void(tx.commitAsync())
          case ExitCase.Error(_)  => async0Void(tx.rollbackAsync())
          case ExitCase.Canceled  => async0Void(tx.rollbackAsync())
        }
    }
  }

  private def session(): Resource[F, Session] = {
    def acquire: F[Session] = F.delay(driver.session())

    def release(session: Session): F[Unit] = F.delay(session.close())

    Resource.make(acquire)(release)
  }

  private def asyncSession(): Resource[F, AsyncSession] = {
    def acquire: F[AsyncSession] = F.delay(driver.asyncSession())

    def release(session: AsyncSession): F[Unit] = async0Void(session.closeAsync())

    Resource.make(acquire)(release)
  }

  private def async0[A](stage: CompletionStage[A]): F[A] = F.async { cb =>
    stage.whenComplete { (t: A, u: Throwable) =>
      if (u != null) {
        cb(Left(u))
      } else {
        cb(Right(t))
      }
    }
  }

  private def async0Void(stage: CompletionStage[Void]): F[Unit] = F.async { cb =>
    stage.whenComplete { (_: Void, u: Throwable) =>
      if (u != null) {
        cb(Left(u))
      } else {
        cb(Right(()))
      }
    }
  }
}

object Neo4jTransactor {
  def create[F[_]: Async](uri: String): Resource[F, Neo4jTransactor[F]] = create(uri, Config.defaultConfig())

  def create[F[_]: Async](uri: URI): Resource[F, Neo4jTransactor[F]] = create(uri, Config.defaultConfig())

  def create[F[_]: Async](uri: String, config: Config): Resource[F, Neo4jTransactor[F]] = create(uri, config, AuthTokens.none())

  def create[F[_]: Async](uri: URI, config: Config): Resource[F, Neo4jTransactor[F]] = create(uri, config, AuthTokens.none())

  def create[F[_]: Async](uri: String, config: Config, authToken: AuthToken): Resource[F, Neo4jTransactor[F]] =
    create(new URI(uri), config, authToken)

  def create[F[_]: Async](uri: URI, config: Config, authToken: AuthToken): Resource[F, Neo4jTransactor[F]] = create0(uri, config, authToken)

  private def create0[F[_]: Async](
    uri: URI,
    config: Config = Config.defaultConfig(),
    authToken: AuthToken = AuthTokens.none()
  ): Resource[F, Neo4jTransactor[F]] = {
    def acquire: F[Driver] = Sync[F].delay(GraphDatabase.driver(uri, authToken, config))

    def release(driver: Driver): F[Unit] = Sync[F].delay(driver.close())

    Resource.make(acquire)(release).map(new Neo4jTransactor[F](_))
  }
}
