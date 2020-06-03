package neo4s.core

import java.util.logging.Level

import com.dimafeng.testcontainers.Neo4jContainer
import com.dimafeng.testcontainers.scalatest.TestContainerForAll
import neo4s.IOSpec
import org.neo4j.driver.{AuthTokens, Config, Logging}
import neo4s._
import neo4s.implicits._

class Neo4jTransactorSpec extends IOSpec with TestContainerForAll {
  override val containerDef: Neo4jContainer.Def = Neo4jContainer.Def(dockerImageName = "neo4j:4.0.0")

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

    "raise error in the middle of transaction -> rollback" in runF {
      val query1 = for {
        summary <- cypher"create (n:Entity {id: 2})".update.run
        _ <- ExecutableOp.raiseError(new RuntimeException("error"))
      } yield {
        assertResult(1)(summary.nodesCreated())
      }

      val query2 = for {
        id <- cypher"match (n:Entity) where n.id = 2 return n.id".query[Int].option
      } yield {
        assert(id.isEmpty)
      }

      for {
        a <- transactor.transact(query1).attempt
        _ <- transactor.transact(query2)
      } yield {
        assert(a.isLeft)
      }
    }

    "pure actions in the middle of transaction" in runF {
      val query = for {
        _ <- cypher"create (n:Entity {id: 3})".update.run
        a <- ExecutableOp.pure(2)
        id <- cypher"match (n:Entity) where n.id = 3 return n.id".query[Int].unique
        r1 = a + id
        b <- ExecutableOp.delay(() => 5)
        r2 = r1 * b
      } yield {
        assertResult(5)(r1)
        assertResult(25)(r2)
      }

      transactor.transact(query)
    }

    "raise error in the middle of transaction and handle it -> commit" in runF {
      val query1 = for {
        summary <- cypher"create (n:Entity {id: 4})".update.run
        _ <- ExecutableOp.handleErrorWith(ExecutableOp.raiseError(new RuntimeException("error")), e => ExecutableOp.unit)
      } yield {
        assertResult(1)(summary.nodesCreated())
      }

      val query2 = for {
        id <- cypher"match (n:Entity) where n.id = 4 return n.id".query[Int].option
      } yield {
        assert(id.isDefined)
      }

      for {
        a <- transactor.transact(query1).attempt
        _ <- transactor.transact(query2)
      } yield {
        assert(a.isRight)
      }
    }

    "manual commit" in runF {
      val query1 = for {
        summary <- cypher"create (n:Entity {id: 5})".update.run
        _ <- ExecutableOp.commit()
      } yield {
        assertResult(1)(summary.nodesCreated())
      }

      val query2 = for {
        id <- cypher"match (n:Entity) where n.id = 5 return n.id".query[Int].option
      } yield {
        assert(id.isDefined)
      }

      for {
        a <- transactor.transact(query1).attempt
        _ <- transactor.transact(query2)
      } yield {
        assert(a.isRight)
      }
    }

    "manual rollback" in runF {
      val query1 = for {
        summary <- cypher"create (n:Entity {id: 6})".update.run
        _ <- ExecutableOp.rollback()
      } yield {
        assertResult(1)(summary.nodesCreated())
      }

      val query2 = for {
        id <- cypher"match (n:Entity) where n.id = 6 return n.id".query[Int].option
      } yield {
        assert(id.isEmpty)
      }

      for {
        a <- transactor.transact(query1).attempt
        _ <- transactor.transact(query2)
      } yield {
        assert(a.isRight)
      }
    }
  }
}
