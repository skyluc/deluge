package org.skyluc.deluge

import spray.can.server.SprayCanHttpServerApp
import akka.actor.Props
import scala.annotation.tailrec

object Server extends SprayCanHttpServerApp {

  def main(args: Array[String]) {
    val handler = system.actorOf(Props[ServerActor], name = "server")
    newHttpServer(handler) ! Bind(interface = "0.0.0.0", port = 4702)

    waitForClosedStdIn()

    println("end of stream")

    Console.in.close()
    Console.out.close()
    system.shutdown()
  }

  @tailrec
  def waitForClosedStdIn() {
    val line = Console.readLine
    if ("quit" != line)
      waitForClosedStdIn()
  }

}