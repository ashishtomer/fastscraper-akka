package com.fastscraping.actors

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import com.fastscraping.actors.PrinterActor.{PrintMessage, PrintStringOnConsole}

class PrinterActor(context: ActorContext[PrintMessage])  extends AbstractBehavior[PrintMessage](context){

  override def onMessage(msg: PrintMessage): Behavior[PrintMessage] = msg match {
    case printMessage @ PrintStringOnConsole =>
      println("Hello on the console")

      Behaviors.same
  }

}

object PrinterActor {
  sealed trait PrintMessage
  case object PrintStringOnConsole extends PrintMessage

  def apply() = Behaviors.setup((context: ActorContext[PrintMessage]) => new PrinterActor(context))
}
