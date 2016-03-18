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

package simx.applications.examples.basic.tutorial.patterns

/**
  * Created by dwiebusch on 17.03.16.
  */
abstract class Subject{
  protected var observers : List[IObserver] = Nil

  def addObserver(o : IObserver): Unit = {
    observers :+= o
  }

  def notifyObservers(msg : Any): Unit ={
    observers.foreach(_.notify(msg))
  }
}

object Observer extends Subject{
  def main(args: Array[String]) {
    addObserver(new MyObserverA("ClassA"))
    addObserver(new MyObserverB("ClassB"))
    addObserver(new MyObserverC("ClassC"))

    notifyObservers(Calculate(1, 2))
  }
}

trait IObserver{
  def notify(msg : Any) : Unit
}

case class Calculate(a : Int, B : Int)

class MyObserverA(name : String) extends IObserver{
  def notify(msg : Any) = msg match {
    case Calculate(a, b) =>
      Thread.sleep(1000)
      println(a + b)
    case otherMsgType => // ignore
  }
}

class MyObserverB(name : String) extends IObserver{
  def notify(msg : Any) = msg match {
    case Calculate(a, b) =>
      Thread.sleep(1000)
      println(a * b)
    case otherMsgType => // ignore
  }
}

class MyObserverC(name : String) extends IObserver{
  def notify(msg : Any) = msg match {
    case Calculate(a, b) =>
      Thread.sleep(1000)
      println(a / b)
    case otherMsgType => // ignore
  }
}



