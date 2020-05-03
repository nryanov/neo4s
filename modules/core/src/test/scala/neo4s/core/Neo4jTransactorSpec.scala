package neo4s.core

import java.util.logging.Level

import com.dimafeng.testcontainers.Neo4jContainer
import com.dimafeng.testcontainers.scalatest.TestContainerForAll
import neo4s.IOSpec
import org.neo4j.driver.{AuthTokens, Config, Logging}
import neo4s._
import neo4s.implicits._

class Neo4jTransactorSpec extends IOSpec with TestContainerForAll {
  override val containerDef = Neo4jContainer.Def(dockerImageName = "neo4j:4.0.0")

  var transactor: Neo4jTransactor[F] = _
  var finisher: F[Unit] = _

  override def afterContainersStart(containers: Neo4jContainer): Unit = {
    val auth = AuthTokens.basic(containers.username, containers.password)
    val config = Config.builder().withLogging(Logging.console(Level.INFO)).build()

    val (transactorR, finisherR) = Neo4jTransactor.create[F](containers.boltUrl, config, auth).allocated.unsafeRunSync()
    transactor = transactorR
    finisher = finisherR
  }

  override def beforeContainersStop(containers: Neo4jContainer): Unit =
    finisher.unsafeRunSync()

  "Neo4j transactor" should {
    "create and return value" in runF {

      val query = for {
        summary <- cypher"create (n:Entity)".update.run
        _ <- cypher"create (n:Entity {id: 1})".update.run
        id <- cypher"match (n:Entity) where n.id = 1 return n.id".query[Int].unique
      } yield {
        assertResult(1)(summary.nodesCreated())
        assertResult(1)(id)
      }

      transactor.transact(query)
    }
  }
}
