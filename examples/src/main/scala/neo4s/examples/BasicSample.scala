package neo4s.examples

import neo4s._
import neo4s.implicits._
import cats.effect.{ExitCode, IO, IOApp}
import cats.syntax.flatMap._

object BasicSample extends IOApp {
  private val NEO4J_URI = "bolt://localhost:7687/db/neo4j"

  case class Address(street: String)
  case class Person(name: String, age: Int, address: Address)

  override def run(args: List[String]): IO[ExitCode] = Neo4jTransactor.create[IO](NEO4J_URI).use { transactor =>
    val name = "Sherlock Holmes"
    val age = 60
    val street = "221B Baker Street"

    val query = for {
      _ <- cypher"create (n:Person {name: $name, age: $age, address: $street})".update.run
      person <- cypher"match (n:Person {name: $name}) return n.name, n.age, n.address".query[Person].unique
    } yield person

    query
      .transact(transactor)
      .flatTap(returnedName => IO.delay(println(s"Returned person: $returnedName")))
      // Returned person: Person(Sherlock Holmes,60,Address(221B Baker Street))
      .map(_ => ExitCode.Success)
      .handleErrorWith(error => IO.delay(println(s"Error: ${error.getLocalizedMessage}")).map(_ => ExitCode.Error))
  }
}
