/*
 * Copyright 2013 The SIRIS Project
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

package simx.applications.examples.basic.remote

import simx.core.svaractor.SVarActor
import simx.core.{ApplicationConfig, SimXApplication, SimXApplicationMain}
import simx.components.renderer.jvr.{JVRConnector, JOGLInit}
import simx.components.physics.jbullet.JBulletComponent
import simx.core.component.remote.RemoteCreation
import simx.components.sound.{LWJGLSoundComponent, OpenALInit}
import simx.core.entity.Entity

/**
 * User: dwiebusch
 * Date: 15.11.13
 * Time: 13:20
 */
object SimxRemoteApplication extends SimXApplicationMain[SimxRemoteApplication]

class SimxRemoteApplication(args : Array[String]) extends SimXApplication with JOGLInit with OpenALInit with RemoteCreation
{
  protected def finishConfiguration(): Unit ={
    registerComponentCreationSupport[LWJGLSoundComponent]("soundNode")
    registerComponentCreationSupport[JBulletComponent]("renderNode")
    registerComponentCreationSupport[JVRConnector]("physicsNode")
  }

  /**
   * Defines the components that [[simx.core.SimXApplication]] has to create
   */
  override protected def applicationConfiguration = ApplicationConfig(Nil)

  protected def configureComponents(components: Map[Symbol, SVarActor.Ref]){}
  protected def createEntities(){}
  protected def removeFromLocalRep(e : Entity){}
}