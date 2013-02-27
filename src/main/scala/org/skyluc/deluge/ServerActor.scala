package org.skyluc.deluge

import akka.actor.Actor
import akka.actor.Props
import akka.event.LoggingReceive
import akka.event.Logging
import spray.http.HttpRequest
import spray.io.IOBridge.Closed

class ServerActor extends Actor {

  val log = Logging(context.system, this)
  
  val router = context.system.actorOf(Props[RouterActor])

  def receive = LoggingReceive {
    case request: HttpRequest =>
      router forward request
    case _: Closed =>
      // nothing to do
    case m =>
      log.info("received unknown message")
  }  
}