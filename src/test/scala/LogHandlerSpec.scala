package systems.miso

import core.database.LogHandler
import core.xo.SessionLogXO

import org.scalatest.BeforeAndAfterAll
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import java.net.InetAddress
import java.time.Instant
import java.time.temporal.ChronoUnit
import scala.jdk.CollectionConverters

class LogHandlerSpec extends AnyFlatSpec with Matchers with BeforeAndAfterAll {
  val logHandler = new LogHandler()
  var sampleLog: SessionLogXO = _

  override def beforeAll(): Unit = {
    sampleLog = SessionLogXO(null, InetAddress.getLocalHost.toString, InetAddress.getLocalHost.toString,
      Instant.now, "SUCCESS", 1000L)
  }

  "A LogHandler" should "successfully write to and read from the database" in {
    logHandler.write(Some(sampleLog))

    // Read the record back from the database and validate
    val readResult = logHandler.read(Some(sampleLog))

    readResult should not be empty
    val readLog = readResult.head
    readLog.getString("_id") should not be null
    readLog.getString("source") shouldEqual sampleLog.source
    readLog.getString("destination") shouldEqual sampleLog.destination
    readLog.getDate("timestamp").toInstant shouldEqual sampleLog.timestamp.truncatedTo(ChronoUnit.MILLIS)
    readLog.getString("result") shouldEqual sampleLog.result
    readLog.getLong("byte_size") shouldEqual sampleLog.byteSize
  }

  it should "handle null logs gracefully" in {
    logHandler.write(None)
    val readResult = logHandler.read(None)
    readResult should be(empty)
  }

  override def afterAll(): Unit = {
  }
}
