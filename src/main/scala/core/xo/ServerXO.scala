package systems.miso
package core.xo

class ServerXO(val id: String, val name: String, val url: String, val port: Int, var connections: Int) {
  override def toString: String = {
    String.format("<ServerXO>%n\t<id>%s</id>%n\t<name>%s</name>%n\t<url>%s</url>%n\t<port>%d</port>%n\t" +
      "<connections>%d</connections>%n</ServerXO>", id, name, url, port, connections)
  }

  def increment: Int = {
    connections += 1
    connections
  }

  def decrement: Int = {
    connections -= 1
    connections
  }
}
