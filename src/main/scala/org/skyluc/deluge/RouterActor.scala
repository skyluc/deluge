package org.skyluc.deluge

import akka.actor.Actor
import akka.event.LoggingReceive
import spray.http.HttpResponse
import akka.actor.ActorRef
import akka.actor.Props
import spray.http.HttpRequest
import spray.http.HttpResponse
import spray.http.StatusCodes
import akka.event.Logging

// messages
case class HandlerDefinition(id: String, actorClass: Class[_ <: Actor], filter: String => Boolean)

// model
case class Handler(actor: ActorRef, filter: String => Boolean)

class RouterActor extends Actor {

  val log = Logging(context.system, this)
  
  private val allRequestsHandler = context.system.actorOf(Props[ListRequestsActor], name = "allRequests")
  private val invalidRequestsHandler = context.system.actorOf(Props[ListRequestsActor], name = "invalidRequests")
  
  var handlers : List[Handler]= List(
      Handler(allRequestsHandler, new StringMatchFilter("/deluge/router/allRequests")),
      Handler(invalidRequestsHandler, new StringMatchFilter("/deluge/router/invalidRequests")));
  
  def receive = LoggingReceive {
    case HandlerDefinition(id, actorClass, filter) =>
      handlers = handlers :+ Handler(context.actorOf(Props(actorClass), name = id), filter)
    case request: HttpRequest =>
      allRequestsHandler ! AddRequestMessage(request)
      handlers.find(_.filter(request.path)) match {
        case Some(handler) =>
          handler.actor forward request
        case None =>
          invalidRequestsHandler ! AddRequestMessage(request)
          sender ! HttpResponse(StatusCodes.NotFound, "%s not found".format(request.uri))
      }
  }
  
}

class StringMatchFilter(path: String) extends Function1[String, Boolean] {
  def apply(toCheck: String): Boolean = path == toCheck
}

case class AddRequestMessage(request: HttpRequest)

class ListRequestsActor extends Actor {
  
  private var requests: Set[String] = Set()
  
  def receive = {
    case _: HttpRequest =>
      sender ! HttpResponse(entity = requests.mkString("\n"))
    case AddRequestMessage(request) =>
      requests += request.path
  }
}