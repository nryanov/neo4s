package neo4s.core.syntax

import cats.effect.Sync
import neo4s.core.ExecutableOp.ExecutableIO
import neo4s.core.Neo4jTransactor

class ExecutableIOOps[A](executableIO: ExecutableIO[A]) {
  def transact[F[_]: Sync](neo4jTransactor: Neo4jTransactor[F]): F[A] = neo4jTransactor.transact(executableIO)
}
