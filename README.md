# neo4s

Neo4s - scala wrapper for [Neo4j](https://github.com/neo4j/neo4j).
The core ideas were taken from [doobie](https://github.com/tpolecat/doobie). 

## Install
```sbt
libraryDependencies ++= Seq(
  "com.nryanov.neo4s" %% "neo4s-core" % "[version]"
)
```

## Usage example
```scala
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
```

## Notes
As this library is just a wrapper for java driver, all threading management is done by underlying driver.

## Custom mappings
By default, `neo4s` provides `Get[A]` and `Put[A]` (`Meta[A]`) for:
- JVM numeric types Byte, Short, Int, Long, Float, and Double;
- BigDecimal and BigInteger;
- Boolean, Char, String, and Array[Byte];
- Date, Time, and Timestamp from the java.sql package;
- LocalDate, LocalTime, LocalDateTime, OffsetTime, OffsetDateTime and ZonedDateTime from the java.time package; and
- List[A], Array[A], Map[String, A]
- single-element case classes wrapping one of the above types.

### Deriving Get and Put for custom type
To derive Get and Put instances for custom type you can use existing Meta[_]:
```scala
sealed abstract class Digit(val value: Int)
case object One extends Digit(1)
case object Two extends Digit(2)
case class Another(value: Int) extends Digit(value)

implicit val digitMeta: Meta[Digit] = Meta[Int].imap {
  case 1 => One
  case 2 => Two
  case x => Another(x)
} {
  digit => digit.value
}
```

Also, you can do the same but using existing Put/Get:
```scala
implicit val digitGet: Get[Digit] = Get[Int].map {
  case 1 => One
  case 2 => Two
  case x => Another(x)
}

implicit val digitPut: Put[Digit] = Put[Int].contramap(digit => digit.value)
```