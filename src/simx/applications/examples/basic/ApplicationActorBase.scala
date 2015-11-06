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

package simx.applications.examples.basic

import simplex3d.math.float._
import simx.components.physics.jbullet.JBulletComponentAspect
import simx.components.renderer.jvr.{JVRComponentAspect, JVRInit}
import simx.components.sound.{LWJGLSoundComponentAspect, OpenALInit}
import simx.core.components.renderer.setup.BasicDisplayConfiguration
import simx.core.worldinterface.eventhandling.{EventHandler, EventProvider}
import simx.core.{ApplicationConfig, SimXApplication}
import simx.core.component.{ExecutionStrategy, Soft}
import simx.core.svaractor.SVarActor
import scala.collection.immutable


/**
 * Created by dennis on 05.12.14.
 */
abstract class ApplicationActorBase extends SimXApplication with JVRInit with OpenALInit with EventProvider with EventHandler
{
  //Component names
  val physicsName = 'physics
  val soundName = 'sound
  val gfxName = 'renderer

  override protected def applicationConfiguration = ApplicationConfig withComponent
    JVRComponentAspect(gfxName, BasicDisplayConfiguration(1280, 800, fullscreen = false)) /*on "renderNode"*/ and
    JBulletComponentAspect(physicsName, ConstVec3(0, -9.81f, 0)) /*on "physicsNode"*/ and
    LWJGLSoundComponentAspect(soundName) /*on "soundNode"*/

  protected def configureComponents(components: immutable.Map[Symbol, SVarActor.Ref]) {
    exitOnClose(components(gfxName), shutdown) // register for exit on close
    start(ExecutionStrategy where
      components(physicsName) runs Soft(60) and
      components(gfxName) runs Soft(60)  and
      components(soundName) runs Soft(60)
    )
  }

  protected def finishConfiguration() { }
}

