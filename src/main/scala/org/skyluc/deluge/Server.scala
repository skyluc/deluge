package org.skyluc.deluge

import spray.can.server.SprayCanHttpServerApp
import akka.actor.Props

object Server extends SprayCanHttpServerApp {

  def main(args: Array[String]) {
    val handler = system.actorOf(Props[ServerActor])
    newHttpServer(handler) ! Bind(interface = "0.0.0.0", port = 4702)
  }

}