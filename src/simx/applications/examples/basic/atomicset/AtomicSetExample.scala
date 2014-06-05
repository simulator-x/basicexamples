/*
 * Copyright 2014 The SIRIS Project
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

package simx.applications.examples.basic.atomicset

import simx.core.entity.Entity
import simx.core.svaractor.{SVarActor, MultiObserve}
import simx.core.svaractor.unifiedaccess.EntityUpdateHandling
import simx.core.svaractor.synclayer.AtomicSetSupport
import simx.core.ontology.types

/**
 * Created by dwiebusch on 10.05.14
 */
object AtomicSetExample {
  case class GotIt( e1 : Entity, e2 : Entity )

  class AnotherActor extends MultiObserve with EntityUpdateHandling{
    override protected def removeFromLocalRep(e: Entity){}
    addHandler[(Entity, Entity)]{
      case (e1, e2) =>
        println("another actor got " + e1 + " and " + e2)
        observe(e1, types.Time, types.Transformation)  onUpdate {
          (values, updated) =>
            val toPrint = updated.map( triple => values.get(triple._2).filter(_._1 equals triple._3).map( _._2 + " -> " + triple._2))
            println(toPrint.mkString("\n\t"))
        }
        val x = sender
        addJobIn(1000) {
          x ! GotIt(e1, e2)
        }
    }

    addHandler[String](println)
  }

  class ExampleActor(anotherActor : SVarActor.Ref) extends AtomicSetSupport with EntityUpdateHandling{

    override protected def removeFromLocalRep(e: Entity){}

    /**
     * called when the actor is started
     */
    override protected def startUp(){
      val e1 = new Entity
      val e2 = new Entity
      e1.set(types.Transformation(simplex3d.math.float.Mat4.Identity))
      e1.set(types.Time(System.currentTimeMillis()))
      e2.set(types.Time(System.currentTimeMillis()))
      anotherActor ! (e1, e2)
      println("starting")
    }

    addHandler[GotIt]{ case GotIt(e1, e2) =>
      println("got that he got it, getting on ;)")
      atomicSet{
        e1.set(types.Transformation(simplex3d.math.float.Mat4.Zero))

        e1.set(types.Time(System.currentTimeMillis()))
      }

      anotherActor ! "more simulated messages from other actor"
      anotherActor ! "more simulated messages from other actor"

      atomicSet{
        e1.set(types.Transformation(simplex3d.math.float.Mat4.Identity))
        anotherActor ! "simulated message from other actor"
        e2.set(types.Time(System.currentTimeMillis()))
      }
    }
  }
  def main (args: Array[String]) {
    SVarActor.createActor(new ExampleActor(SVarActor.createActor(new AnotherActor)))
  }
}


