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

package simx.applications.examples.basic.cv

import simx.core.{ApplicationConfig, SimXApplication, SimXApplicationMain}
import simx.components.editor.EditorComponentAspect
import scala.collection.immutable
import simx.core.svaractor.SVarActor
import simx.core.ontology.EntityDescription
import simx.core.worldinterface.naming.NameIt
import simx.components.io.cv.OpenCvComponentAspect
import simx.components.io.cv.aspects.Camera
import simplex3d.math.ConstVec2i
import simx.core.entity.Entity

/**
 * Created by IntelliJ IDEA.
 * User: martin
 * Date: 21/01/14
 * Time: 11:18
 */
object CvExample extends SimXApplicationMain[CvExample]

class CvExample(args : Array[String]) extends SimXApplication {

  val editorName = 'editor
  val openCvName = 'openCv

  /**
   * Defines the components that [[simx.core.SimXApplication]] has to create
   */
  override protected def applicationConfiguration = ApplicationConfig withComponent
    EditorComponentAspect(editorName) and
    OpenCvComponentAspect(openCvName)

  protected def configureComponents(components: immutable.Map[Symbol, SVarActor.Ref]) {
    println("[Configuring components]")
  }

  protected def createEntities() {
    println("[Creating entities]")
    new EntityDescription(Camera(0, ConstVec2i(320,240)), NameIt("Camera")).realize()
  }

  protected def finishConfiguration() {
    println("[Configuring application]")
  }

  protected def removeFromLocalRep(e : Entity){

  }
}