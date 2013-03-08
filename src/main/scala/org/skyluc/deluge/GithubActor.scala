package org.skyluc.deluge

import akka.actor.Actor
import akka.actor.ActorSystem
import akka.actor.Props
import akka.actor.actorRef2Scala
import spray.http.HttpRequest
import spray.http.HttpResponse

object GithubActor {
  
  def init(system: ActorSystem) {
    val router= system.actorFor("/user/router")
    
    router ! HandlerDefinition(system.actorOf(Props[GithubActor], name = "github"), new StringMatchFilter("/deluge/github/webhook"))
  }
  
}

class GithubActor extends Actor {
  
  override def receive = {
    case _: HttpRequest =>
      sender ! HttpResponse()
  }

}