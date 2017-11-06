package pme.bots.control

import javax.inject.{Inject, Named, Singleton}

import akka.actor.ActorRef
import pme.bots.entity.SubscrType.SubscrAspect
import pme.bots.entity.{RunAspect, Subscription}
/**
  * Created by pascal.mengelt on 14.01.2017.
  */
@Singleton
class LogStateSubscription @Inject()(@Named("commandDispatcher")
                                     val commandDispatcher: ActorRef) {
  commandDispatcher ! Subscription(RunAspect.logStateCommand, SubscrAspect, None)

}
