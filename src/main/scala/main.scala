package systems.miso

import core.TransceiverServer

import org.apache.logging.log4j.{LogManager, Logger}

@main
def main(): Unit = {
  val logger: Logger = LogManager.getFormatterLogger()

  Runtime.getRuntime.addShutdownHook(new Thread {
    override def run(): Unit = {
      logger.warn("Received signal to shutdown... Starting now!")
      TransceiverServer.close()
    }
  })

  val transThread: Thread = new Thread(TransceiverServer)
  transThread.start()

  // logger.warn(s"${Thread.currentThread().getName} will now sleep for 30 seconds!")
  // Thread.sleep(30000)
  // TransceiverServer.printStatistics()
}
