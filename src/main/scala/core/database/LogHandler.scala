package systems.miso
package core.database

import core.config.MongoConfiguration
import core.xo.SessionLogXO

import com.mongodb.*
import com.mongodb.MongoCredential.*
import com.mongodb.client.internal.MongoClientImpl
import com.mongodb.client.result.InsertOneResult
import com.mongodb.client.{MongoClient, MongoClients, MongoCollection, MongoDatabase}
import org.apache.logging.log4j.{LogManager, Logger}
import org.bson.*

import java.util
import java.util.concurrent.TimeUnit
import scala.concurrent
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}
import scala.jdk.CollectionConverters.*
import scala.util.Using

class LogHandler {
  private val logger: Logger = LogManager.getFormatterLogger()
  private val config: MongoConfiguration = new MongoConfiguration
  private val credential: MongoCredential = createCredential(config.getUser, config.getDatabase, config.getSecret.toCharArray)
  private val connectionURL: String = config.getURL + config.getPort
  private val serverAPI: ServerApi = ServerApi.builder.version(ServerApiVersion.V1).build()
  private val settings: MongoClientSettings = MongoClientSettings
    .builder()
    .credential(credential)
    .applyConnectionString(new ConnectionString(connectionURL))
    .serverApi(serverAPI)
    .build()

  private val client: MongoClient = MongoClients.create(settings)

  private def init(): Unit = {
    try {
      val database = client.getDatabase(config.getDatabase)
      val ping = database.runCommand(new BsonDocument("ping", new BsonInt32(1)))
      logger.info("Successfully pinged the Mongo database!")
    } catch {
      case e: MongoException =>
        logger.error(s"Failed to ping database due to MongoDB error: ${e.getMessage}")
      case e: Exception =>
        logger.error(s"Failed to ping database due to generic error: ${e.getMessage}")
    }
  }

  def write(sessionLog: Option[SessionLogXO]): Unit = {
    sessionLog.foreach { log =>
      try {
        val database: MongoDatabase = client.getDatabase(config.getDatabase)
        val collection: MongoCollection[Document] = database.getCollection(config.getCollection)
        val result: InsertOneResult = collection.insertOne(log.toDocument)
        logger.info("Successfully inserted session log!")
      } catch {
        case e: MongoException =>
          logger.error(s"Failed to insert session log due to MongoDB error: ${e.getMessage}")
        case e: Exception =>
          logger.error(s"Failed to insert session log due to generic error: ${e.getMessage}")
      }
    }
  }

  def read(searchDocument: Option[SessionLogXO]): List[Document] = {
    try {
      val database: MongoDatabase = client.getDatabase(config.getDatabase)
      val collection: MongoCollection[Document] = database.getCollection(config.getCollection)

      val result: List[Document] = searchDocument match {
        case Some(v) =>
          val searchWithoutId = v.toDocument
          searchWithoutId.remove("_id")

          logger.info(s"Searching with the following search data: ${searchWithoutId.toJson}")

          val findIterable = collection.find(searchWithoutId)
          findIterable.into(new util.ArrayList[Document]()).asScala.toList
        case None =>
          logger.info("Did not find document!")
          List.empty[Document]
      }
      result
    } catch {
      case e: MongoException =>
        logger.error(s"Failed to read session log due to MongoDB error: ${e.getMessage}")
        List.empty[Document]
      case e: Exception =>
        logger.error(s"Failed to read session log due to generic error: ${e.getMessage}")
        List.empty[Document]
    }
  }

  init()
}

