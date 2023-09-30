package systems.miso
package core.xo

import org.bson.*

import java.time.Instant
import java.util.UUID

class SessionLogXO(var _id: String, var source: String, var destination: String, var timestamp: Instant,
                   var result: String, var byteSize: Long) {
  private def init(): Unit = {
    if (_id == null || _id.isBlank) {
      _id = UUID.randomUUID().toString
    }
  }

  def toDocument: Document = {
    new Document()
      .append("_id", new BsonString(_id))
      .append("source", new BsonString(source))
      .append("destination", new BsonString(destination))
      .append("timestamp", new BsonDateTime(timestamp.toEpochMilli))
      .append("result", new BsonString(result))
      .append("byte_size", new BsonInt64(byteSize))
  }

  init()
}
