package systems.miso
package core.config

import org.apache.logging.log4j.{LogManager, Logger}

import java.io.InputStream
import scala.util.Using
import scala.xml.XML

class MongoConfiguration extends Configuration {
  private val logger: Logger = LogManager.getFormatterLogger()

  private var _url: String = _
  private var _port: Int = _
  private var _database: String = _
  private var _collection: String = _
  private var _user: String = _
  private var _secret: String = _

  override def init(): Unit = {
    var stream: InputStream = null

    try {
      stream = getClass.getResourceAsStream("/mongo.xml")

      if (stream != null) {
        val xml = XML.load(stream)

        _url = (xml \ "url").text
        _port = (xml \ "port").text.toInt
        _database = (xml \ "database").text
        _collection = (xml \ "collection").text
        _user = (xml \ "user").text
        _secret = (xml \ "secret").text
      } else {
        logger.info("Resource stream is null.")
      }
    } catch {
      case e: Exception =>
        logger.info(s"Exception occurred: ${e.getMessage}")
    } finally {
      if (stream != null) {
        stream.close()
      }
    }
  }

  def getURL: String = _url

  def getPort: Int = _port

  def getDatabase: String = _database

  def getCollection: String = _collection

  def getUser: String = _user

  def getSecret: String = _secret

  init()
}
