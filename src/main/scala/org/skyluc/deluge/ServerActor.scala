package org.skyluc.deluge

import akka.actor.Actor
import akka.actor.Props
import akka.event.LoggingReceive
import akka.event.Logging
import spray.http.HttpRequest
import spray.io.IOBridge.Closed
import spray.http.IllegalRequestException
import spray.http.HttpResponse
import spray.http.StatusCodes

class ServerActor extends Actor {

  private val log = Logging(context.system, this)

  private val router = context.system.actorOf(Props[RouterActor], name = "router")

  def receive = {
    case request: HttpRequest =>
      try {
        val parsedRequest = request.parseAll
        router forward parsedRequest
      } catch {
        case e: IllegalRequestException =>
          log.error(e, "Exception while parsing request")
          sender ! HttpResponse(StatusCodes.InternalServerError, entity = "Something went wrong. Sorry.")
      }
    case _: Closed =>
    // nothing to do
    case m =>
      log.info("received unknown message")
  }
}