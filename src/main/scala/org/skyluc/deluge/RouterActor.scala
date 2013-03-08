package org.skyluc.deluge

import scala.xml.Xhtml

import akka.actor.Actor
import akka.actor.ActorRef
import akka.actor.Props
import akka.actor.actorRef2Scala
import akka.event.Logging
import akka.event.LoggingReceive
import spray.http.ContentType
import spray.http.HttpBody
import spray.http.HttpEntity.apply
import spray.http.HttpRequest
import spray.http.HttpResponse
import spray.http.MediaTypes
import spray.http.StatusCodes

object RouterActor {
  // model
  
  private case class Handler(actor: ActorRef, filter: PathFilter)

  // messages

  private case class AddRequestMessage(request: HttpRequest)
  private case class AddFilterMessage(filter: PathFilter)

  // actors

  private class ListRequestsActor extends Actor {

    private var requests: Set[String] = Set()

    override def receive = {
      case _: HttpRequest =>
        sender ! HttpResponse(entity = requests.mkString("\n"))
      case AddRequestMessage(request) =>
        requests += request.path
    }
  }

  private class ListAvailableActor extends Actor {
    private var filters: List[PathFilter] = List()

    override def receive = {
      case _: HttpRequest =>
        val content =
          <html>
            <body>
              {
                filters.map {
                  f: PathFilter =>
                    <p><a href={ f.displayString }>{ f.displayString }</a></p>
                }
              }
            </body>
          </html>
        sender ! HttpResponse(entity = HttpBody(ContentType(MediaTypes.`text/html`), Xhtml.toXhtml(content)))
      case AddFilterMessage(filter) =>
        filters = filters :+ filter
    }
  }
}

// messages
case class HandlerDefinition(actor: ActorRef, filter: PathFilter)

class RouterActor extends Actor {
  import RouterActor._

  private val log = Logging(context.system, this)

  private val allRequestsHandler = context.system.actorOf(Props[ListRequestsActor], name = "allRequests")
  private val invalidRequestsHandler = context.system.actorOf(Props[ListRequestsActor], name = "invalidRequests")
  private val listAvailableHandler = context.system.actorOf(Props[ListAvailableActor], name = "listAvailable")

  private var handlers: List[Handler] = List()

  self ! HandlerDefinition(listAvailableHandler, new StringMatchFilter("/deluge/router/list"))
  self ! HandlerDefinition(allRequestsHandler, new StringMatchFilter("/deluge/router/allRequests"))
  self ! HandlerDefinition(invalidRequestsHandler, new StringMatchFilter("/deluge/router/invalidRequests"))

  override def receive = LoggingReceive {
    case HandlerDefinition(actor, filter) =>
      handlers = handlers :+ Handler(actor, filter)
      listAvailableHandler ! AddFilterMessage(filter)
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

trait PathFilter extends Function1[String, Boolean] {
  def displayString: String
}

class StringMatchFilter(path: String) extends PathFilter {
  override def apply(toCheck: String): Boolean = path == toCheck

  override def displayString = path
}
