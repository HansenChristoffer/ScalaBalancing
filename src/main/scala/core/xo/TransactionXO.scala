package systems.miso
package core.xo

import org.apache.logging.log4j.{LogManager, Logger}

import java.io.{BufferedInputStream, BufferedOutputStream, InputStream, OutputStream}
import java.net.Socket
import java.util.UUID

object TransactionXO {
  def apply(sock: Socket): TransactionXO = new TransactionXO(sock)
}

class TransactionXO(val sock: Socket) extends AutoCloseable {
  private val logger: Logger = LogManager.getFormatterLogger()
  private val transactionId: UUID = UUID.randomUUID()
  private val inboundBuffer: InputStream = new BufferedInputStream(sock.getInputStream)
  private val outboundBuffer: OutputStream = new BufferedOutputStream(sock.getOutputStream)
  var readBytes: Int = _

  def getInboundBuffer: InputStream = {
    inboundBuffer
  }

  def getOutboundBuffer: OutputStream = {
    outboundBuffer
  }

  def getTransactionId: UUID = {
    transactionId
  }

  override def close(): Unit = {
    logger.warn(s"Closing down TransactionXO#${transactionId}!")

    if (sock.isConnected) {
      inboundBuffer.close()
      outboundBuffer.close()

      sock.close()
    }
  }
}
