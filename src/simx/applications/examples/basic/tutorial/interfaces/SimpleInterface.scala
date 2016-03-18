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

package simx.applications.examples.basic.tutorial.interfaces

/**
  * Created by dwiebusch on 17.03.16.
  */
object SimpleInterface {
  def main(args: Array[String]) {
    var calculator : Calculation = new ClassA
    println(calculator.calculate(1, 2))
    calculator = new ClassB
    println(calculator.calculate(1, 2))
    calculator = new ClassC
    println(calculator.calculate(1, 2))
  }
}

trait Calculation{
  def calculate(a : Int, b: Int) : Int
}

class ClassA extends Calculation {
  def calculate(a : Int, b: Int) = {
    Thread.sleep(1000)
    a + b
  }
}

class ClassB extends Calculation {
  def calculate(a : Int, b: Int) = {
    Thread.sleep(1000)
    a * b
  }
}

class ClassC extends Calculation {
  def calculate(a : Int, b: Int) = {
    Thread.sleep(1000)
    a / b
  }
}


// TODO: rename


