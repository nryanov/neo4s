import java.net.URI

import cats.effect.{Resource, Sync}
import org.neo4j.driver._

package object neo4s {
  trait Transactor[F[_]]

  final class Neo4sDriver[F[_]: Sync](driver: Driver) {}

  object Neo4sDriver {
    def create[F[_]: Sync](uri: String): Resource[F, Neo4sDriver[F]] = create(uri, Config.defaultConfig())

    def create[F[_]: Sync](uri: URI): Resource[F, Neo4sDriver[F]] = create(uri, Config.defaultConfig())

    def create[F[_]: Sync](uri: String, config: Config): Resource[F, Neo4sDriver[F]] = create(uri, config, AuthTokens.none())

    def create[F[_]: Sync](uri: URI, config: Config): Resource[F, Neo4sDriver[F]] = create(uri, config, AuthTokens.none())

    def create[F[_]: Sync](uri: String, config: Config, authToken: AuthToken): Resource[F, Neo4sDriver[F]] =
      create(new URI(uri), config, authToken)

    def create[F[_]: Sync](uri: URI, config: Config, authToken: AuthToken): Resource[F, Neo4sDriver[F]] = create0(uri, config, authToken)

    private def create0[F[_]: Sync](
      uri: URI,
      config: Config = Config.defaultConfig(),
      authToken: AuthToken = AuthTokens.none()
    ): Resource[F, Neo4sDriver[F]] = {
      def acquire: F[Driver] = Sync[F].delay(GraphDatabase.driver(uri, authToken, config))

      def release(driver: Driver): F[Unit] = Sync[F].delay(driver.close())

      Resource.make(acquire)(release).map(new Neo4sDriver[F](_))
    }
  }

  trait Query[A]

  var driver: Driver = _

  val result: Result = driver.session().run("", Values.parameters())

  val next: Record = result.next()
}
