/*
 * Copyright 2012 The SIRIS Project
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

package simx.applications.basicexamples.cluster

import simx.components.renderer.jvr.{JVRInit, JVRConnector}
import simx.components.physics.jbullet.JBulletComponent
import simx.core.components.physics.PhysicsConfiguration
import simx.core.components.renderer.messages._
import simx.core.helper.Execute
import simx.core.SimXClusterApplication
import simx.core.component.{Soft, ExecutionStrategy}
import simx.applications.basicexamples.objects.{Ball, Light, Table}
import collection.immutable
import simx.core.svaractor.SVarActor
import simplex3d.math.floatx.{ConstVec3f, Vec3f}

/**
 * A clustered version of [[simx.applications.basicexamples.ExampleApplication]].
 *
 * This application simulated the same scene as [[simx.applications.basicexamples.ExampleApplication]] but is using
 * a display description that is describing two displays connected to different cluster nodes.
 *
 * @author Stephan Rehfeld
 * @author Dennis Wiebusch
 *
 * @param bootstrapApp Pass 'true' if this instance should run the bootstrap code. Typically the return value of
 *                     [[simx.core.SimXClusterApplication.startClusterSubsystem]].
 */
class ExampleClusterApplication( bootstrapApp : Boolean ) extends SimXClusterApplication( bootstrapApp ) with JVRInit {

  var renderer : Option[SVarActor.Ref] = None

  override def startUp() {
    super.startUp()
  }

  override protected def createComponents() {
    println("creating components")
    // create components

    createActor(new JBulletComponent())( (physics) => {
      println( "  physics created")
      createActor( new JVRConnector())( (renderer) => {
        println( "  renderer created" )

        this.renderer = Some( renderer )

        // send configs
        renderer ! ConfigureRenderer( ClusterDisplayDescription(800, 600),
          effectsConfiguration =  EffectsConfiguration( "low","none" ) )

        physics ! PhysicsConfiguration (ConstVec3f(0, -9.81f, 0))

        // register for exit on close:
        exitOnClose(renderer, shutdown)

        this.addJobIn(5000){
          val executionStrategy = ExecutionStrategy where physics runs Soft( 60 ) and renderer runs Soft( 60 )
          this.start( executionStrategy )

          componentsCreated()
        }
      })()
    })()

  }

  protected def configureComponents(components: immutable.Map[Symbol, SVarActor.Ref]) {}

  override protected def createEntities() {
    println("creating entities")

    Execute serialized
      realize( Table("the table", Vec3f(3f, 1f, 2f), Vec3f(0f, -1.5f, -7f) ) ) and
      realize( Light("the light", Vec3f(-4f, 8f, -7f), Vec3f(270f, -25f, 0f) ) ) and
      realize( Ball ("the ball", 0.2f,  Vec3f(0f, 1f, -7f) ) ) exec( x => {

    })
  }

  override protected def finishConfiguration() {
    println("application is running")
  }
}

/**
 * This object contains the main method to start the ExampleClusterApplication.
 *
 * @author Stephan Rehfeld
 */
object ExampleClusterApplication {
  def main( args : Array[String] ) {
    println( "----------------- SimulatorX Cluster Examples: Example Application -------------" )
    println( "| This application simulated the same scene as ExampleApplication but is using |" )
    println( "| a display description that is describing two displays connected to different |" )
    println( "| cluster nodes.                                                               |" )
    println( "|                                                                              |" )
    println( "|   Suggested start parameter to run on one machine:                           |" )
    println( "| #1 --bootstrap-app --name front --interface 127.0.0.1 --port 9000            |" )
    println( "| #2 --name back --interface 127.0.0.1 --port 9001 --seed-node 127.0.0.1:9000  |" )
    println( "--------------------------------------------------------------------------------" )

    def clusterNodes = Set('front, 'back)
    val bootstrapApp = SimXClusterApplication.startClusterSubsystem( args, clusterNodes )
    SVarActor.createActor(new ExampleClusterApplication( bootstrapApp ))
  }
}