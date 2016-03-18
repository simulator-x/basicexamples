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

package simx.applications.examples.basic.component

import simx.components.renderer.jvr._
import simx.components.renderer.jvr.JVRInit
import simx.core.ontology.EntityDescription
import simx.core.{ApplicationConfig, SimXApplicationMain, SimXApplication}
import simx.core.component.{Soft, ExecutionStrategy}
import simx.core.components.renderer.messages.{ConfigureRenderer, EffectsConfiguration}
import simx.core.components.renderer.createparameter.ShapeFromFile
import simx.applications.examples.basic.objects.Light

import collection.immutable
import simplex3d.math.floatx.{Mat4x3f, ConstVec3f,  ConstMat4f}
import simx.core.svaractor.SVarActor
import simx.core.components.renderer.setup.BasicDisplayConfiguration
import simx.core.entity.Entity
import simx.core.components.physics.ImplicitEitherConversion._

/**
 *
 * The object responsible for creating a new instance of this application
 *
 * @author Dennis Wiebusch
 */
object BasicComponentApplication extends SimXApplicationMain(new BasicComponentApplication)

/**
 * An application providing an example for a self written component
 * (see [[simx.applications.examples.basic.component.ExampleComponent]] and
 * [[simx.applications.examples.basic.component.ExampleSphereAspect]])
 *
 * @author Dennis Wiebusch
 */
class BasicComponentApplication extends SimXApplication with JVRInit{
  //define component names
  private val exampleComponent  = 'exampleComponent
  private val rendererComponent = 'renderer

  protected def applicationConfiguration = ApplicationConfig withComponent
    JVRComponentAspect(rendererComponent) and
    ExampleComponentAspect(exampleComponent)


  protected def removeFromLocalRep(e : Entity){

  }

  /**
   * called after all components were created
   * @param components the map of components, accessible by their names
   */
  protected def configureComponents(components: immutable.Map[Symbol, SVarActor.Ref]) {
    // get access to components
    val (renderer, exampleComp) = (components(rendererComponent), components(exampleComponent))

    // register for exit on close:
    exitOnClose(renderer, shutdown)

    // start components
    start(ExecutionStrategy where renderer runs Soft(60) and exampleComp runs Soft(180)  )
  }

  case class ExampleBall(name : String, radius : Float, position : ConstVec3f) extends EntityDescription(
    // define information for the ExampleComponent
    ExampleSphereAspect(
      // assign an id for the ExampleComponent
      id             = Symbol("myId")
    ),
    // define information fot the renderer
    ShapeFromFile(
      // assign the ball.dae collada file for shape and coloring information
      file           = "assets/vis/ball.dae",
      //
      scale          = ConstMat4f(Mat4x3f.scale(radius * 0.5f)),
      // set the transfiormation of the ball
      transformation = ConstMat4f(Mat4x3f.translate(position))
    )
  )

  /**
   * called when the entities are meant to be created
   */
  protected def createEntities() {
    // create a light to make objects in the scene visible
    Light( name = "the light", pos = ConstVec3f(-4f, 8f, -2f), rot = ConstVec3f(270f, -25f, 0f)) realize {
      light => println("created new light")
    }

    // create a ball which is rendered and known by the example component
    ExampleBall( name = "the ball", radius = 0.2f, position =  ConstVec3f(0.25f, 0f, -2f)) realize {
      ball => println("created new ball")
    }
  }

  /**
   * called after components were configured and the creation of entities was initiated
   */
  protected def finishConfiguration() {
    println("application is running")
  }
}