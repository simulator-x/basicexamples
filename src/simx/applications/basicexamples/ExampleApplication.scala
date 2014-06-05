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

package simx.applications.basicexamples

import objects._
import simx.components.renderer.jvr.{JVRInit, JVRConnector}
import simx.components.physics.jbullet.JBulletComponent
import simx.components.editor.{EditorConfiguration, Editor}
import simx.core.{SimXApplicationMain, SimXApplication}
import simx.core.component.{Triggered, ExecutionStrategy, Soft}
import simx.core.components.renderer.createparameter.ReadFromElseWhere
import simx.core.components.physics.ImplicitEitherConversion._
import simx.core.entity.Entity
import simx.core.ontology.EntityDescription
import simx.core.ontology.types
import collection.immutable
import util.Random
import simx.components.sound.{OpenALInit, LWJGLSoundComponent}
import simplex3d.math.floatx._
import simx.core.components.renderer.createparameter.ShapeFromFile
import simx.core.components.physics.PhysicsConfiguration
import simx.core.components.renderer.messages.EffectsConfiguration
import simx.components.sound.SoundMaterial
import simx.core.components.physics.PhysSphere
import simx.core.components.renderer.messages.ConfigureRenderer
import simx.applications.basicexamples.objects.Ball
import simx.applications.basicexamples.objects.Light
import simx.applications.basicexamples.objects.Table
import simx.core.components.naming.NameIt
import simx.core.svaractor.SVarActor

/**
 * TODO: Document
 * @author Dennis Wiebusch, Martin Fischbach
 */
object ExampleApplication extends SimXApplicationMain( new ExampleApplication )

class ExampleApplication extends SimXApplication with JVRInit with OpenALInit {
  val gfxName = 'renderer
  val physicsName = 'physics
  val editorName = 'editor
  val soundName = 'sound

  val useEditor = false

  protected def createComponents() {
    println("[Creating components]")

    create(new LWJGLSoundComponent(soundName))
    create(new JBulletComponent(physicsName))
    create(new JVRConnector(gfxName))
    if (useEditor) create(new Editor(editorName))
  }

  protected def configureComponents(components: immutable.Map[Symbol, SVarActor.Ref]) {
    println("[Configuring components]")

    components(gfxName) ! ConfigureRenderer(BasicDisplayConfiguration(1280, 800, fullscreen = false), EffectsConfiguration("low","none"))

    exitOnClose(components(gfxName), shutdown) // register for exit on close

    start(ExecutionStrategy where components(soundName) runs Soft(60))

    components(physicsName) ! PhysicsConfiguration (ConstVec3f(0, -9.81f, 0))
    start(ExecutionStrategy where
      components(physicsName) runs Triggered() and
      components(gfxName) runs Triggered() where
      components(physicsName) isTriggeredBy components(gfxName) and
      components(gfxName) isTriggeredBy components(physicsName) startWith Set(components(physicsName)) )

    if (useEditor) components(editorName) ! EditorConfiguration(appName = "MasterControlProgram")
  }

  private var tableEntityOption: Option[Entity] = None
  private val ballRadius = 0.2f
  private val ballPosition = ConstVec3f(0f, 1.5f, -7f)

  protected def createEntities() {
    println("[Creating entities]")

    Sounds.init()

    val ballDescription =
      new EntityDescription (
        PhysSphere(
          restitution    = 0.998f,
          transform      = ballPosition,
          radius         = ballRadius
        ),
        ShapeFromFile(
          file           = "assets/vis/ball.dae",
          scale          = ConstMat4f(Mat4x3f.scale(ballRadius*2f)),
          transformation = ReadFromElseWhere
        ),
        SoundMaterial("ball"),
        NameIt("the ball")
      )

    ballDescription.realize(entityComplete)
    Light("the light", Vec3f(-4f, 8f, -7f), Vec3f(270f, -25f, 0f)).realize(entityComplete)
    Table("the table", Vec3f(3f, 1f, 2f), Vec3f(0f, -1.5f, -7f)).realize((tableEntity: Entity) => {
      entityComplete(tableEntity)
      tableEntityOption = Some(tableEntity)
    })
  }

  private def entityComplete(e: Entity) {println("[Completetd entity] " + e)}

  protected def finishConfiguration() {
    println("[Configuring application]")
    rotateTable()
    initializeBallSpawning()
    initializeMouseControl()
    println("[Application is running] Press SPACE to spawn new balls!")
  }

  private def rotateTable() {
    addJobIn(16L){
      tableEntityOption.collect{ case tableEntity => {
        //In complex applications it is reasonable to check if the list is not empty, rather than just call 'head'
        val transformationSVar = tableEntity.get(types.Transformation).head
        transformationSVar.get(
          (currentTransform) => {transformationSVar.set(rotate(currentTransform))})
      }}
      rotateTable()
    }
  }

  private def rotate(mat: ConstMat4f) = mat * ConstMat4f(Mat4x3f.rotateY(0.01f))

  private def initializeBallSpawning() {
    handleDevice(types.Keyboard)( (keyboardEntity) => {
      val spaceSVar = keyboardEntity.get(types.Key_Space).head
      spaceSVar.observe( (pressed) => {if(pressed) spawnBall()} )
    })
  }

  private var ballCounter = 0

  private def spawnBall() {
    ballCounter += 1
    val randomOffset = Vec3f(Random.nextFloat(), Random.nextFloat(), Random.nextFloat()) * 0.05f
    Ball("ball#" + ballCounter, ballRadius, ballPosition + randomOffset) realize {
      newBallEntity => newBallEntity.get(types.Transformation).head.observe {
        newTransform => if(extractHeight(newTransform) < -2f) newBallEntity.remove()
      }
    }
  }

  private def extractHeight(mat: ConstMat4f) = mat.m31

  private var userEntityOption: Option[Entity] = None

  def doIt(userEntity : Entity){
    userEntityOption = Some(userEntity)
  }

  private def initializeMouseControl() {


    handleDevice(types.User)(doIt)
    handleDevice(types.Mouse)( (mouseEntity) => {
      val positionSVar = mouseEntity.get(types.Position2D).head
      positionSVar.observe{ newMousePosition => {
        userEntityOption.collect{ case userEntity =>
         val viewplattformTransformationSvar = userEntity.get(types.ViewPlatform).head
         viewplattformTransformationSvar.set(calculateView(newMousePosition))
        }
      }}
    })
  }

  private def calculateView(mousePos: ConstVec2f) = {
    val weight = 0.1f
    val angleHorizontal = ((mousePos.x - 400f) / -400f) * weight
    val angleVertical = ((mousePos.y - 300f) / -300f) * weight
    ConstMat4f(Mat4x3f.rotateY(angleHorizontal).rotateX(angleVertical))
  }
}