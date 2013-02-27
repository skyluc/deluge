package org.skyluc.deluge

import akka.actor.Actor
import akka.event.LoggingReceive
import spray.http.HttpResponse

class RouterActor extends Actor {

  def receive = LoggingReceive {
    case _ =>
      sender ! HttpResponse(entity = "ok")
  }
  
}