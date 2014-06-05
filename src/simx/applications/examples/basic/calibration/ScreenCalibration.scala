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

package simx.applications.examples.basic.calibration

/**
 * Created by dwiebusch on 19.05.14
 */
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

import simx.components.renderer.jvr.{JVRPickEvent, JVRComponentAspect, JVRInit}
import simx.core.{ApplicationConfig, SimXApplicationMain, SimXApplication}
import simx.core.component.{Soft, ExecutionStrategy}
import simx.core.components.renderer.createparameter.ReadFromElseWhere
import simx.core.components.physics.ImplicitEitherConversion._
import simx.core.entity.Entity
import simx.core.ontology.{Symbols, types, EntityDescription}
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
import simx.core.components.renderer.setup.ConfigurableDisplayConfiguration




/**
 * TODO: Document
 * @author Dennis Wiebusch, Martin Fischbach
 */
object ScreenCalibration extends SimXApplicationMain[ScreenCalibration] {
  val useEditor = askForOption("Use Editor Component?")
}

class ScreenCalibration(args : Array[String]) extends SimXApplication
with JVRInit with OpenALInit with RemoteCreation with EventProvider with EventHandler
{
  //Component names
  val physicsName = 'physics
  val editorName = 'editor
  val soundName = 'sound
  val gfxName = 'renderer

  val displaySetupDesc = new ConfigurableDisplayConfiguration
  private var rendererEntity : Option[Entity] = None

  override protected def applicationConfiguration = ApplicationConfig withComponent
    JVRComponentAspect(gfxName, displaySetupDesc.getDescription) /*on "renderNode"*/ and
    JBulletComponentAspect(physicsName, ConstVec3(0, -9.81f, 0)) /*on "physicsNode"*/ and
    LWJGLSoundComponentAspect(soundName) /*on "soundNode"*/ and
    EditorComponentAspect(editorName, appName = "MasterControlProgram") iff ScreenCalibration.useEditor

  protected def configureComponents(components: immutable.Map[Symbol, SVarActor.Ref]) {
    println("configured renderer with " + displaySetupDesc)
    exitOnClose(components(gfxName), shutdown) // register for exit on close
    start(ExecutionStrategy where
      components(physicsName) runs Soft(60) and
      components(gfxName) runs Soft(60)  and
      components(soundName) runs Soft(60)
      //where
      //components(physicsName) isTriggeredBy components(gfxName) and
      //components(gfxName) isTriggeredBy components(physicsName) startWith Set(components(physicsName))
    )

    addJobIn(5000){
      handleRegisteredEntities(Symbols.component.value.toSymbol :: Symbols.graphics.value.toSymbol:: gfxName :: Nil){
        entities => rendererEntity = entities.headOption
      }
    }
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

    new EntityDescription("thePlane",
      ShapeFromFile(
        file = "models/plane.dae",
        transformation = ConstMat4(Mat4x3.translate(Vec3(0,0,-0.6f)))
      )
    ).realize()

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
    setupKeyBindings()
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
      _.get(types.Entity).collect{
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
        tableEntity.get(types.Transformation).first(
          currentTransform => tableEntity.set(types.Transformation(rotate(currentTransform))))
      }
      rotateTable()
    }
  }

  private def rotate(mat: ConstMat4) =
    mat * ConstMat4(Mat4x3.rotateY(0.01f))

  private def setupKeyBindings() {
    handleDevice(types.User){
      e => e.set(types.ViewPlatform(ConstMat4(Mat4x3.translate(Vec3(0,0,2f)))))
    }

    handleDevice(types.Keyboard){ keyboardEntity =>
      keyboardEntity.observe(types.Key_Space).first( pressed => if(pressed) spawnBall() )
      keyboardEntity.observe(types.Key_t).first{ pressed =>
        if (pressed) {
          displaySetupDesc.setFullscrean(!displaySetupDesc.getFullscrean)
          updateDisplayDesc()
        }
      }
      keyboardEntity.observe(types.Key_Left).first{ pressed =>
        if (pressed) {
          displaySetupDesc.setScreenTransform(displaySetupDesc.getScreenTransform * ConstMat4(Mat4x3.translate(Vec3(-0.005f, 0, 0))))
          updateDisplayDesc()
        }
      }
      keyboardEntity.observe(types.Key_Right).first{ pressed =>
        if (pressed) {
          displaySetupDesc.setScreenTransform(displaySetupDesc.getScreenTransform * ConstMat4(Mat4x3.translate(Vec3(0.005f, 0, 0))))
          updateDisplayDesc()
        }
      }
      keyboardEntity.observe(types.Key_Up).first{ pressed =>
        if (pressed) {
          displaySetupDesc.setScreenTransform(displaySetupDesc.getScreenTransform * ConstMat4(Mat4x3.translate(Vec3(0, 0.005f, 0))))
          updateDisplayDesc()
        }
      }
      keyboardEntity.observe(types.Key_Down).first{ pressed =>
        if (pressed) {
          displaySetupDesc.setScreenTransform(displaySetupDesc.getScreenTransform * ConstMat4(Mat4x3.translate(Vec3(0, -0.005f, 0))))
          updateDisplayDesc()
        }
      }

      keyboardEntity.observe(types.Key_a).first{ pressed =>
        if (pressed) {
          val screenSize = displaySetupDesc.getSizeOfScreen
          displaySetupDesc.setSizeOfScreen(screenSize._1 - 0.105, screenSize._2)
          updateDisplayDesc()
        }
      }
      keyboardEntity.observe(types.Key_d).first{ pressed =>
        if (pressed) {
          val screenSize = displaySetupDesc.getSizeOfScreen
          displaySetupDesc.setSizeOfScreen(screenSize._1 + 0.105, screenSize._2)
          updateDisplayDesc()
        }
      }
      keyboardEntity.observe(types.Key_w).first{ pressed =>
        if (pressed) {
          val screenSize = displaySetupDesc.getSizeOfScreen
          displaySetupDesc.setSizeOfScreen(screenSize._1, screenSize._2 + 0.105)
          updateDisplayDesc()
        }
      }
      keyboardEntity.observe(types.Key_s).first{ pressed =>
        if (pressed) {
          val screenSize = displaySetupDesc.getSizeOfScreen
          displaySetupDesc.setSizeOfScreen(screenSize._1, screenSize._2 - 0.105)
          updateDisplayDesc()
        }
      }


    }
  }

  private def updateDisplayDesc(){
    rendererEntity.collect{
      case entity =>
        println("updating to " + displaySetupDesc)
        entity.set(types.DisplaySetupDescription(displaySetupDesc.getDescription))
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

    }
  }
}
