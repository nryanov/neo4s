# neo4s

Neo4s - scala wrapper for [Neo4j](https://github.com/neo4j/neo4j).

## Build
```shell script
sbt clean compile
```

## Test
```shell script
sbt test
```

## Usage example
```scala
import neo4s._
import neo4s.implicits._
import cats.effect.{ExitCode, IO, IOApp}
import cats.syntax.flatMap._

object BasicSample extends IOApp {
  private val NEO4J_URI = "bolt://localhost:7687/db/neo4j"

  override def run(args: List[String]): IO[ExitCode] = Neo4jTransactor.create[IO](NEO4J_URI).use { transactor =>
    val name = "Name"

    val query = for {
      _ <- cypher"create (n:Person {name: $name})".update.run
      name <- cypher"match (n:Person {name: $name}) return n.name".query[String].unique
    } yield name

    query
      .transact(transactor)
      .flatTap(list => IO.delay(println(s"List size: ${list.length}")))
      .flatTap(returnedName => IO.delay(println(s"Returned name: $returnedName")))
      .map(_ => ExitCode.Success)
      .handleErrorWith(error => IO.delay(println(s"Error: ${error.getLocalizedMessage}")).map(_ => ExitCode.Error))
  }
}
```