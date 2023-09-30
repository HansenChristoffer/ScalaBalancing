package systems.miso
package core.config

import core.xo.ServerXO

import org.apache.logging.log4j.{LogManager, Logger}

import java.io.InputStream
import java.util.UUID
import scala.xml.XML

object HostConfiguration {
  def apply(): HostConfiguration = new HostConfiguration()
}

class HostConfiguration extends Configuration {
  private val logger: Logger = LogManager.getFormatterLogger()

  private var serverXOList: List[ServerXO] = List.empty[ServerXO]

  override def init(): Unit = {
    var stream: InputStream = null

    try {
      stream = getClass.getResourceAsStream("/hostconfiguration.xml")
      if (stream != null) {
        val xml = XML.load(stream)

        // Update this to reassign serverXOList
        serverXOList = (xml \ "ServerXO").map { node =>
          val id = (node \ "id").text
          val name = (node \ "name").text
          val url = (node \ "url").text
          val port = (node \ "port").text.toInt

          ServerXO(id, name, url, port, 0)
        }.toList
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

  def listOfServerXO(): List[ServerXO] = {
    serverXOList
  }

  init()
}
