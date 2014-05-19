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

import simx.components.renderer.jvr.{JVRPickEvent, JVRComponentAspect, JVRInit}
import simx.core.{ApplicationConfig, SimXApplicationMain, SimXApplication}
import simx.core.component.{Soft, ExecutionStrategy}
import simx.core.components.renderer.createparameter.ReadFromElseWhere
import simx.core.components.physics.ImplicitEitherConversion._
import simx.core.entity.Entity
import simx.core.ontology.{types, EntityDescription}
import simx.components.sound.{SoundMaterial, LWJGLSoundComponentAspect, OpenALInit}
import simx.core.components.renderer.createparameter.ShapeFromFile
import simx.core.components.physics.PhysSphere
import simx.applications.examples.basic.objects.{Sounds, Ball, Light, Table}
import simx.core.svaractor.SVarActor
import simx.core.component.remote.RemoteCreation
import simx.core.worldinterface.naming.NameIt
import simx.components.physics.jbullet.JBulletComponentAspect
import simx.components.editor.EditorComponentAspect
import simplex3d.math.float._
import collection.immutable
import util.Random
import simx.core.worldinterface.eventhandling.{EventHandler, EventProvider}
import simx.core.components.io.SpeechEvents

/**
 * TODO: Document
 * @author Dennis Wiebusch, Martin Fischbach
 */
object ExampleApplication extends SimXApplicationMain[ExampleApplication] {
  val useEditor = askForOption("Use Editor Component?")
}

class ExampleApplication(args : Array[String]) extends SimXApplication
with JVRInit with OpenALInit with RemoteCreation with EventProvider with EventHandler
{
  //Component names
  val physicsName = 'physics
  val editorName = 'editor
  val soundName = 'sound
  val gfxName = 'renderer

  override protected def applicationConfiguration = ApplicationConfig withComponent
    JVRComponentAspect(gfxName) /*on "renderNode"*/ and
    JBulletComponentAspect(physicsName, ConstVec3(0, -9.81f, 0)) /*on "physicsNode"*/ and
    LWJGLSoundComponentAspect(soundName) /*on "soundNode"*/ and
    EditorComponentAspect(editorName, appName = "MasterControlProgram") iff ExampleApplication.useEditor

  protected def configureComponents(components: immutable.Map[Symbol, SVarActor.Ref]) {
    exitOnClose(components(gfxName), shutdown) // register for exit on close
    start(ExecutionStrategy where
      components(physicsName) runs Soft(60) and
      components(gfxName) runs Soft(60)  and
      components(soundName) runs Soft(60)
      //where
      //components(physicsName) isTriggeredBy components(gfxName) and
      //components(gfxName) isTriggeredBy components(physicsName) startWith Set(components(physicsName))
    )
  }

  private var tableEntityOption: Option[Entity] = None
  private val ballRadius = 0.2f
  private val ballPosition = ConstVec3(0f, 1.5f, -7f)

  protected def createEntities() {
    Sounds.init()

    val ballDescription =
      new EntityDescription ("theBall",
        PhysSphere(
          restitution    = 0.998f,
          transform      = ballPosition,
          radius         = ballRadius
        ),
        ShapeFromFile(
          file           = "assets/vis/ball.dae",
          scale          = ConstMat4(Mat4x3.scale(ballRadius*2f)),
          transformation = ReadFromElseWhere
        ),
        SoundMaterial("ball"),
        NameIt("the ball")
      )

    ballDescription.realize(entityComplete)
    Light("the light", Vec3(-4f, 8f, -7f), Vec3(270f, -25f, 0f)).realize(entityComplete)
    Table("the table", Vec3(3f, 1f, 2f), Vec3(0f, -1.5f, -7f)).realize((tableEntity: Entity) => {
      entityComplete(tableEntity)
      tableEntityOption = Some(tableEntity)
    })
  }

  private def entityComplete(e: Entity) {println("[info][ExampleApplication] Completed entity " + e)}


  protected def removeFromLocalRep(e : Entity){println("[info][ExampleApplication] Removed entity " + e)}

  protected def finishConfiguration() {
    rotateTable()
    initializePicking()
    initializeBallSpawning()
    initializeMouseControl()
    SpeechEvents.token.observe{ event =>
      val text = event.values.firstValueFor(types.String)
      println("[info][ExampleApplication] Test event received. Contained sting is: " + text)
    }
    println("[info][ExampleApplication] Application is running: Press SPACE to spawn new balls!")
  }

  private def initializePicking(){
    var clickedOnce = Set[Entity]()
    JVRPickEvent.observe{
      event => if (event.get(types.Enabled).getOrElse(false)) event.get(types.Entity).collect{
        case entity if !clickedOnce.contains(entity) =>
          println("picked " + entity)
          clickedOnce = clickedOnce + entity
          entity.set(types.Velocity(Vec3.Zero))
          entity.disableAspect(PhysSphere())
        case entity =>
          println("picked " + entity)
          clickedOnce = clickedOnce - entity
          entity.enableAspect(PhysSphere())
      }
    }
  }

  private def rotateTable() {
    addJobIn(16L){
      tableEntityOption.collect{ case tableEntity =>
        //In complex applications it is reasonable to check if the list is not empty, rather than just call 'head'
        tableEntity.get(types.Transformation).first(
          currentTransform => tableEntity.set(types.Transformation(rotate(currentTransform))))
      }
      rotateTable()
    }
  }

  private def rotate(mat: ConstMat4) =
    mat * ConstMat4(Mat4x3.rotateY(0.01f))

  private def initializeBallSpawning() {
    handleDevice(types.Keyboard){ keyboardEntity =>
      keyboardEntity.observe(types.Key_Space).first( pressed => if(pressed) spawnBall() )
      keyboardEntity.observe(types.Key_t).first{ pressed =>
        if(pressed) {
          println("[info][ExampleApplication] Test event emitted")
          SpeechEvents.token.emit(types.String("test"), types.Time(10L))
        }
      }
    }
  }

  private var ballCounter = 0

  private def spawnBall() {
    ballCounter += 1
    val randomOffset = Vec3(Random.nextFloat(), Random.nextFloat(), Random.nextFloat()) * 0.05f
    Ball("ball#" + ballCounter, ballRadius, ballPosition + randomOffset) realize {
      newBallEntity => newBallEntity.observe(types.Transformation).first {
        newTransform => if(extractHeight(newTransform) < -2f) newBallEntity.remove()
      }
    }
  }

  private def extractHeight(mat: ConstMat4) = mat.m31

  private var userEntityOption: Option[Entity] = None

  def doIt(userEntity : Entity){
    userEntityOption = Some(userEntity)
  }

  private def initializeMouseControl() {
    handleDevice(types.User)(doIt)
    handleDevice(types.Mouse){ mouseEntity =>
      mouseEntity.observe(types.Position2D).first{
        newMousePosition => userEntityOption.collect{
          case userEntity => userEntity.set(types.ViewPlatform(calculateView(newMousePosition)))
        }
      }
    }
  }

  private def calculateView(mousePos: ConstVec2) = {
    val weight = 0.1f
    val angleHorizontal = ((mousePos.x - 400f) / -400f) * weight
    val angleVertical = ((mousePos.y - 300f) / -300f) * weight
    ConstMat4(Mat4x3.rotateY(angleHorizontal).rotateX(angleVertical))
  }
}