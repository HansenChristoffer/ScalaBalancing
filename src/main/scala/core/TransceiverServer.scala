package systems.miso
package core

import core.config.{HostConfiguration, TransceiverConfiguration}
import core.handler.ConnectionHandler
import core.xo.{ServerXO, TransactionXO}

import org.apache.logging.log4j.{LogManager, Logger}

import java.net.{ServerSocket, Socket}

object TransceiverServer extends Runnable, AutoCloseable {
  private val logger: Logger = LogManager.getFormatterLogger()
  private val transceiverConfig = TransceiverConfiguration()
  private val serverSock: ServerSocket = new ServerSocket(transceiverConfig.getPort)
  private val hostConfig: HostConfiguration = HostConfiguration()
  private val serverList: List[ServerXO] = hostConfig.listOfServerXO()
  private val destinationHosts: List[ConnectionHandler] = serverList
    .map(b => ConnectionHandler(b.id, new Socket(b.url, b.port)))

  private var shouldRun: Boolean = true
  private var pauseBool: Boolean = false

  private def init(): Unit = {
    if (!serverSock.isClosed) {
      logger.info(s"TransceiverServer is up and running and listening on port, ${serverSock.getLocalPort}")
    }

    destinationHosts.foreach(x => logger.info("::: Hosts :::\n" + x.toString))
  }

  override def run(): Unit = {
    while (shouldRun) {
      if (!pauseBool) {
        val sock: Socket = serverSock.accept()
        sock.setKeepAlive(true)
        sock.setTcpNoDelay(true)
        hostWithSmallestQueue().addToQueue(TransactionXO(sock))
      }
    }
  }

  def pause(): Unit = {
    pauseBool = true
  }

  def printStatistics(): Unit = {
    // Find the length of the longest UUID
    val maxLength: Int = destinationHosts.map(_._id.length).max(Ordering[Int])

    val headerFormat: String = s"%-${maxLength}s | Queue Size%n"
    val rowFormat: String = s"%-${maxLength}s | %-10d%n"

    // Print header
    print(f"Statistics for the objects:\n")
    print(f"----------------------------\n")
    printf(headerFormat, "UUID")

    // Print each row
    destinationHosts.foreach { obj =>
      printf(rowFormat, obj._id, obj.queueSize)
    }
  }

  private def hostWithSmallestQueue(): ConnectionHandler = {
    destinationHosts.minBy(_.queueSize)(Ordering[Int])
  }

  override def close(): Unit = {
    logger.warn(s"TransceiverServer is shutting down!")

    if (destinationHosts.nonEmpty) {
      destinationHosts.foreach(dest => dest.close())
    }

    shouldRun = false
  }

  init()
}
