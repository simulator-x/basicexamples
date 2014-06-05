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

package simx.applications.examples.basic

import simx.core.svaractor.SVarActor
import simx.components.physics.jbullet.JBulletComponent
import simx.components.renderer.jvr.JVRConnector
import simx.core.component.{ExecutionStrategyHandling, ExecutionStrategy}
import simx.core.entity.component.EntityCreationHandling
import simx.core.components.renderer.createparameter.ReadFromElseWhere
import simx.core.components.physics.ImplicitEitherConversion._
import java.awt.Color
import simx.core.components.renderer.setup._
import simx.core.ontology.EntityDescription
import simx.core.SimXConfig
import simplex3d.math.floatx._
import simx.core.components.renderer.createparameter.ShapeFromFile
import simx.core.components.physics.PhysicsConfiguration
import simx.core.components.renderer.messages.EffectsConfiguration
import simx.core.component.Soft
import simx.core.components.physics.PhysSphere
import simx.core.components.physics.PhysBox
import scala.Some
import simx.core.components.renderer.messages.ConfigureRenderer
import simx.core.components.renderer.createparameter.SpotLight
import simx.core.worldinterface.naming.NameIt
import simx.core.entity.Entity


/**
 * This application simulated the same scene as [[simx.applications.examples.basic.ExampleApplication]] but is a
 * implementation. It is not using many abstractions. It should illustrate the bare API of Simulator X.
 *
 * @author Stephan Rehfeld
 */
class BareExampleApplication extends SVarActor with ExecutionStrategyHandling with EntityCreationHandling with SimXConfig{


  protected def removeFromLocalRep(e : Entity){

  }

  override protected def startUp() {
    super.startUp()

    createActor( new JBulletComponent( Symbol("physics"), 1000, 10, 1.0f / (60.0f * 2.0f)))( physics => {
      println( "  physics created")
      createActor( new JVRConnector())( renderer => {
        println( "  renderer created" )
        // TODO : Commented out editor. Fix it and comment it in
        //createActor[Editor](Symbol("editor"))( editor => {

        // send configs

        physics ! PhysicsConfiguration (ConstVec3f(0, -9.81f, 0))
        //editor ! EditorConfiguration(appName = "MasterControlProgram")

        val displayDesc = new DisplayDesc(
          resolution = Some( 800 -> 600 ),
          size = 0.423 -> 0.317,
          transformation = ConstMat4f( Mat4x3f.translate( Vec3f( 0.0f, 0.0f, -0.6f ) ) ),
          view = new CamDesc(
            camId = 0,
            eye = Eye.RightEye,
            eyeSeparation = Some( 0.0f )
          )
        )
        val displaySetup = new DisplaySetupDesc().addDevice(
          dev = new DisplayDevice(
            hardwareHandle = None,
            displayDescs = displayDesc :: Nil,
            linkType = LinkType.SingleDisplay ),
          groupId = 0
        )

        renderer ! ConfigureRenderer(
          displaySetup = displaySetup,
          effectsConfiguration = EffectsConfiguration( "low","none" )
        )

        val executionStrategy = ExecutionStrategy where
          physics runs Soft( 60 ) and
          renderer runs Soft( 60 )
        this.start( executionStrategy )


        new EntityDescription(
          PhysBox(
            halfExtends    = Vec3f(1.5f, 0.5f, 1f),
            transform      = ConstVec3f(0f, -1.5f, -7f),
            restitution    = 0.998f,
            mass           = 0f
          ),
          ShapeFromFile(
            transformation = ReadFromElseWhere,
            scale          = ConstMat4f(Mat4x3f.translate(ConstVec3f(0,1,0)).scale(ConstVec3f(1f, 0.5f, 2f))),
            file           = "assets/vis/table.dae"
          ),
          NameIt("Table")
        ).realize{ table =>

          new EntityDescription(
            PhysSphere(
              restitution    = 0.998f,
              transform      = ConstVec3f(0f, 1.5f, -7f),
              radius         = 0.2f
            ),
            ShapeFromFile(
              file           = "assets/vis/ball.dae",
              scale          = ConstMat4f(Mat4x3f.scale(0.4f)),
              transformation = ReadFromElseWhere
            ),
            NameIt("Ball")
          ).realize{ ball =>

            new EntityDescription(
              SpotLight(
                name = "light",
                transformation = ConstMat4f( Mat4x3f.
                  rotateZ(functions.radians(0f)).
                  rotateY(functions.radians(-25f)).
                  rotateX(functions.radians(270f)).
                  translate(Vec3f(-4f, 8f, -7f))
                ),
                diffuseColor   = new Color(0.5f, 0.6f, 0.5f),
                specularColor  = new Color(0.5f, 0.6f, 0.5f)
              ),
              NameIt("Light")
            ).realize( )
          }
        }
        //})()
      })()
    })()

  }

}

/**
 * This object contains the main method to start the BareExampleApplication.
 *
 * @author Stephan Rehfeld
 */
object BareExampleApplication {

  def main( args : Array[String] ) {
    println( "------------------- SimulatorX Examples: Bare Example Application --------------" )
    println( "| This application simulated the same scene as ExampleApplication but is a     |" )
    println( "| implementation. It is not using many abstractions. It should illustrate      |" )
    println( "| the bare API of Simulator X.                                                 |" )
    println( "--------------------------------------------------------------------------------" )
    SVarActor.createActor(new BareExampleApplication())
  }
}
