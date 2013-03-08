package org.skyluc.deluge

import akka.actor.Actor
import akka.actor.ActorSystem
import akka.actor.Props
import akka.actor.actorRef2Scala
import akka.event.Logging
import spray.http.HttpRequest
import spray.http.HttpResponse

object GithubActor {
  
  def init(system: ActorSystem) {
    val router= system.actorFor("/user/router")
    
    router ! HandlerDefinition(system.actorOf(Props[GithubActor], name = "github"), new StringMatchFilter("/deluge/github/webhook"))
  }
  
}

class GithubActor extends Actor {
  
  private val log = Logging(context.system, this)

  override def receive = {
    case request: HttpRequest =>
      
      log.debug(request.entity.asString)
      
      sender ! HttpResponse()
  }

}