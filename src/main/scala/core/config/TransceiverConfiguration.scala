package systems.miso
package core.config

import org.apache.logging.log4j.{LogManager, Logger}

import java.io.InputStream
import scala.xml.XML

object TransceiverConfiguration {
  def apply(): TransceiverConfiguration = new TransceiverConfiguration()
}

class TransceiverConfiguration extends Configuration {
  private val logger: Logger = LogManager.getFormatterLogger()

  private var _PORT: Int = _

  override def init(): Unit = {
    var stream: InputStream = null

    try {
      stream = getClass.getResourceAsStream("/transceiverconfiguration.xml")

      if (stream != null) {
        val xml = XML.load(stream)
        _PORT = (xml \ "port").text.toInt
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

  def getPort: Int = {
    _PORT
  }

  init()
}
