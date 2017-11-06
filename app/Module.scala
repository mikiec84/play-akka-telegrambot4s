import com.google.inject.AbstractModule
import play.api.{Logger => PlayLogger}
import play.api.libs.concurrent.AkkaGuiceSupport
import pme.bots.boundary.BotRunner
import pme.bots.control.{CommandDispatcher, LogStateSubscription}
import pme.bots.examples.conversations.CounterServiceSubscription
import pme.bots.examples.services.HelloServiceSubscription

/**
  * This class is a Guice module that tells Guice how to bind several
  * different types. This Guice module is created when the Play
  * application starts.
  *
  */
class Module
  extends AbstractModule
    with AkkaGuiceSupport {
  private val log = PlayLogger(getClass)

  override def configure() {
    log.info("config modules")
    bindActor[CommandDispatcher]("commandDispatcher")
    bind(classOf[BotRunner]).asEagerSingleton()
    bind(classOf[HelloServiceSubscription]).asEagerSingleton()
    bind(classOf[CounterServiceSubscription]).asEagerSingleton()
    bind(classOf[LogStateSubscription]).asEagerSingleton()

  }

}
