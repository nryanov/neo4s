package neo4s

trait Aliases extends Types with Modules

trait Types {
  type Meta[A] = neo4s.utils.Meta[A]
  type Put[A] = neo4s.utils.Put[A]
  type Get[A] = neo4s.utils.Get[A]
  type Read[A] = neo4s.utils.Read[A]
  type Write[A] = neo4s.utils.Write[A]

  type Neo4jTransactor[F[_]] = neo4s.core.Neo4jTransactor[F]

  type PreparedQuery[A] = neo4s.core.PreparedQuery[A]
  type PreparedAction = neo4s.core.PreparedAction

  type ExecutableOp[A] = neo4s.core.ExecutableOp[A]
  type ExecutableIO[A] = neo4s.core.ExecutableOp.ExecutableIO[A]

  type CypherQuery = neo4s.core.CypherQuery
  type Element = neo4s.core.Element
}

trait Modules {
  val Meta = neo4s.utils.Meta
  val Put = neo4s.utils.Put
  val Get = neo4s.utils.Get
  val Read = neo4s.utils.Read
  val Write = neo4s.utils.Write

  val Neo4jTransactor = neo4s.core.Neo4jTransactor

  val PreparedQuery = neo4s.core.PreparedQuery
  val PreparedAction = neo4s.core.PreparedAction

  val ExecutableOp = neo4s.core.ExecutableOp
}
