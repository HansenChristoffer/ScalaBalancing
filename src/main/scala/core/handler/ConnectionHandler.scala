package systems.miso
package core.handler

import core.database.LogHandler
import core.xo.{SessionLogXO, TransactionXO}

import org.apache.logging.log4j.{LogManager, Logger}

import java.io.{BufferedInputStream, BufferedOutputStream}
import java.net.Socket
import java.time.Instant
import java.util.UUID
import java.util.concurrent.{BlockingQueue, ConcurrentLinkedQueue, Executors, LinkedBlockingQueue}
import scala.concurrent.{ExecutionContext, ExecutionContextExecutorService, Future}
import scala.util.{Failure, Success}

object ConnectionHandler {
  def apply(_id: String, sock: Socket): ConnectionHandler = new ConnectionHandler(_id, sock)
}

class ConnectionHandler(val _id: String, val destination: Socket) extends BaseHandler, AutoCloseable {
  private val logger: Logger = LogManager.getFormatterLogger()
  private val PACKET_SIZE: Short = 512

  private implicit val ec: ExecutionContextExecutorService =
    ExecutionContext.fromExecutorService(Executors.newFixedThreadPool(10))

  private val connectionQueue: BlockingQueue[TransactionXO] = new LinkedBlockingQueue[TransactionXO]()

  def addToQueue(transConnectionXO: TransactionXO): Unit = {
    logger.info(s"Connection [${transConnectionXO.getTransactionId}] has been added to ConnectionHandler#${_id} queue!")
    connectionQueue.add(transConnectionXO)
  }

  private def pollQueue(): Option[TransactionXO] = {
    Option(connectionQueue.take())
  }

  def queueSize: Int = connectionQueue.size()

  def close(): Unit = {
    logger.warn(s"ConnectionHandler#${_id} shutting down!")
    ec.shutdown()

    if (!connectionQueue.isEmpty) {
      connectionQueue.forEach(v => v.close())
    }

    if (!destination.isClosed) {
      destination.close()
    }
  }

  private def processConnection(transConnectionXO: TransactionXO): Future[Unit] = Future {
    val destinationOutbound = new BufferedOutputStream(destination.getOutputStream)
    val destinationInbound = new BufferedInputStream(destination.getInputStream)

    val receivedBytes: Array[Byte] = Array.fill[Byte](PACKET_SIZE)(0)
    transConnectionXO.readBytes = transConnectionXO.getInboundBuffer.read(receivedBytes)

    logger.info(s"Received from source: ${receivedBytes.slice(0, transConnectionXO.readBytes).mkString("Array(", ", ", ")")}")

    if (receivedBytes.nonEmpty) {
      destinationOutbound.write(receivedBytes)
      destinationOutbound.flush()
      logger.info(s"Transmitting to destination: ${receivedBytes.slice(0, transConnectionXO.readBytes).mkString("Array(", ", ", ")")}")

      val response = Array.fill[Byte](PACKET_SIZE)(0) // Create a byte array of size 512 (or however large you expect the response to be)
      val bytesRead = destinationInbound.read(response) // Read the bytes into the array; returns the number of bytes read
      logger.info(s"Received as response from destination: ${response.slice(0, bytesRead).mkString("Array(", ", ", ")")}")

      if (response.nonEmpty) {
        transConnectionXO.getOutboundBuffer.write(response)
        transConnectionXO.getOutboundBuffer.flush()
        logger.info(s"Transmitting response to source: ${response.slice(0, bytesRead).mkString("Array(", ", ", ")")}")
      }
    }
  }

  private def handleConnections(): Unit = {
    Future {
      while (!Thread.currentThread().isInterrupted) {
        pollQueue().foreach { transConnectionXO =>
          Future {
            processConnection(transConnectionXO)
          }.onComplete {
            case Success(_) => {
              logger.info(s"Successfully processed connection! [${transConnectionXO.getTransactionId}]")
              val logHandler: LogHandler = new LogHandler
              val sessionLogOption: Option[SessionLogXO] = Some(new SessionLogXO(null, transConnectionXO.sock.getInetAddress.toString,
                destination.getInetAddress.toString, Instant.now, "SUCCESS", transConnectionXO.readBytes))
              logHandler.write(sessionLogOption)
            }
            case Failure(t) => {
              logger.error(s"Failed to process connection! [${transConnectionXO.getTransactionId}]: ${t}")
              val logHandler: LogHandler = new LogHandler
              val sessionLogOption: Option[SessionLogXO] = Some(new SessionLogXO(null, transConnectionXO.sock.getInetAddress.toString,
                destination.getInetAddress.toString, Instant.now, "FAILURE", transConnectionXO.readBytes))
              logHandler.write(sessionLogOption)
            }
          }
        }
      }
    }
  }

  override def toString: String = {
    s"{\n\t_id: ${_id},\n\tsock: {\n\t\tisClosed: ${destination.isClosed},\n\t\tinetAddress: ${destination.getInetAddress.toString}\n\t}\n}"
  }

  handleConnections()
}
