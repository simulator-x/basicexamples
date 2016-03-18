/*
 * Copyright 2016 The SIRIS Project
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 *
 * The SIRIS Project is a cooperation between Beuth University, Berlin and the
 * HCI Group at the University of Würzburg. The project is funded by the German
 * Federal Ministry of Education and Research (grant no. 17N4409).
 */

package simx.applications.examples.basic.tutorial.actors

import akka.actor._

/**
  * Created by dwiebusch on 17.03.16.
  */
object Actors {
  private val system = ActorSystem("TestActorSystem")
  private var actors : List[ActorRef] = Nil

  private def createActor[T <: Actor : reflect.ClassTag](ctor : => T) =
    actors :+= system.actorOf(Props(ctor), reflect.classTag[T].runtimeClass.getSimpleName)

  private def notify(msg : Any): Unit =
    actors.foreach(_ ! msg)

  private val controller = system.actorOf(Props apply new Actor {
    def receive = {
      case Result(r) => handleResult(sender, r)
      case _ =>
    }
  })

  def handleResult(sender : ActorRef, value : Int): Unit ={
    println(value)
    actors = actors.filterNot(_ == sender)
    if (actors.isEmpty)
      system.shutdown
  }

  def main(args: Array[String]) {
    createActor(new ActorA(controller))
    createActor(new ActorB(controller))
    createActor(new ActorC(controller))

    notify(Calculate(1, 2))
  }
}

case class Calculate(a : Int, b : Int)
case class Result(r : Int)

class ActorA(controller : ActorRef) extends akka.actor.Actor{
  def receive = {
    case Calculate(a, b) =>
      Thread.sleep(1000)
      controller ! Result(a + b)
  }
}

class ActorB(controller : ActorRef) extends akka.actor.Actor{
  def receive = {
    case Calculate(a, b) =>
      Thread.sleep(1000)
      controller ! Result(a * b)
  }
}

class ActorC(controller : ActorRef) extends akka.actor.Actor{
  def receive = {
    case Calculate(a, b) =>
      Thread.sleep(1000)
      controller ! Result(a / b)
  }
}
