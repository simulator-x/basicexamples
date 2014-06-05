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

package simx.applications.basicexamples.syncgroup

import simx.components.renderer.jvr.{JVRInit, JVRConnector}
import simx.components.physics.jbullet.JBulletComponent
import simx.core.SimXApplication
import simx.core.helper.Execute
import simx.core.component._
import simx.applications.basicexamples.BasicDisplayConfiguration
import simx.core.svaractor.SVarActor
import simx.core.entity.Entity
import simx.core.components.physics.PhysicsConfiguration
import simx.core.components.renderer.messages.EffectsConfiguration
import simx.core.component.Soft
import scala.Some
import simx.core.components.renderer.messages.ConfigureRenderer
import simx.applications.basicexamples.objects.Ball
import simx.applications.basicexamples.objects.Light
import simx.applications.basicexamples.objects.Table
import simx.core.svaractor.synclayer.{SyncGroupHandling, SyncGroup, SyncGroupCreationHandling}
import collection.immutable
import simplex3d.math.floatx.{ConstVec3f, Vec3f}


/**
 * This application simulated the same scene as [[simx.applications.basicexamples.ExampleApplication]] but is starting
 * a second actor that is observing a sync group. The jumping ball is added to the sync group.
 *
 * @author Stephan Rehfeld
 * @author Dennis Wiebusch
 */
object ExampleSyncGroupApplication{
  def main( String : Array[String] ) { SVarActor.createActor(new ExampleSyncGroupApplication) }
}

class ExampleSyncGroupApplication extends SimXApplication with JVRInit with SyncGroupCreationHandling {


  private var renderer : Option[SVarActor.Ref] = None
  private var syncGroup : Option[SyncGroup] = None

  override protected def createComponents() {
    println("creating components")
    // create components

    createActor( new JBulletComponent( Symbol("physics"), 1000, 10, 1.0f / (60.0f * 2.0f)))( physics => {
      println( "  physics created")
      createActor( new JVRConnector())( renderer => {
        println( "  renderer created" )
        this.renderer = Some( renderer )
        // TODO : Commented out editor. Fix it and comment it in
        //createActor[Editor](Symbol("editor"))( editor => {

          // send configs
          renderer ! ConfigureRenderer( BasicDisplayConfiguration(800, 600),
            effectsConfiguration = EffectsConfiguration( "low","none" )
          )

          physics ! PhysicsConfiguration (ConstVec3f(0, -9.81f, 0))
        //  editor ! EditorConfiguration(appName = "MasterControlProgram")

          // register for exit on close:
          exitOnClose(renderer, shutdown)

          val syncGroupDescription = SyncGroup withLeader physics onSymbols simx.core.ontology.types.Transformation

          introduce( syncGroupDescription )( (syncGroup) => {
            val executionStrategy = ExecutionStrategy where physics runs Soft( 60 ) and renderer runs Soft( 60 )
            this.start( executionStrategy )
            this.syncGroup = Some( syncGroup )
            componentsCreated()
          })


        //})()
      })()
    })()
  }

  protected def configureComponents(components: immutable.Map[Symbol, SVarActor.Ref]) {}

  override protected def createEntities() {
    println("creating entities")
    Execute inParallel
      realize( Table("the table", Vec3f(3f, 1f, 2f), Vec3f(0f, -1.5f, -7f) )) and
      realize( Light("the light", Vec3f(-4f, 8f, -7f), Vec3f(270f, -25f, 0f) ) ) and
      realize( Ball ("the ball", 0.2f,  Vec3f(0f, 1.5f, -7f) ) ) exec {
      case (_, ball) => {
        add( ball, syncGroup.get )( (e,syncGroup) => {
          createActor( new SyncedActor( ball, syncGroup ))( (a) => {} )()
        })()
      }
    }


  }

  override protected def finishConfiguration() {
    println("application is running")
  }
}

/**
 * This actor observes a given [[simx.core.svaractor.synclayer.SyncGroup]]. It observes the transformation in the given
 * entity and prints out the current value. It steps forward every second.
 *
 * @author Stephan Rehfeld
 *
 * @param entity The entity with the transformation sVar to observe.
 * @param syncGroup The sync group to observe.
 */
class SyncedActor( entity : Entity, syncGroup : SyncGroup ) extends SVarActor with SyncGroupHandling {

  require( entity != null, "The parameter 'entity' must not be 'null'!" )
  require( entity.get( simx.core.ontology.types.Transformation ).nonEmpty,
    "The 'entity' must contain a transformation!" )
  require( syncGroup != null, "The parameter 'entity' must not be 'null'!" )

  override def startUp() {
    syncOn( syncGroup, (x:SyncGroup) => {
      print( "New world step available, but value is still " )
      entity.get(simx.core.ontology.types.Transformation).head.get( (t) => println( t ) )} )
    entity.get( simx.core.ontology.types.Transformation ).head.observe(
      (t) => println( "World stepped forward and new value is: " + t ) )
    addJobIn( 1000)( update() )
  }

  /**
   * This method call the stepForward function of the sync group subsystem and adds a new job in 1 second.
   */
  private def update() {
    stepForward()
    addJobIn( 1000)( update() )
  }

}