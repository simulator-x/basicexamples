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

package simx.applications.basicexamples.component

import simx.core.ontology.{GroundedSymbol, Symbols}
import simx.core.ontology.types.{Transformation, Identifier}
import simx.core.entity.typeconversion.{ Converter, ConvertibleTrait}
import simx.core.entity.description._
import simx.core.entity.Entity
import simx.core.svaractor.SVar
import simx.core.component.Component
import simx.core.entity.description.NamedSValSet
import simx.applications.basicexamples.ontology.types.{Transformation => LocalTrafo}
import simplex3d.math.floatx.{ConstMat4f, ConstVec3f, functions, Mat4x3f}

/**
 * Object to define some component related values
 */
object ExampleComponent{
  val componentType = Symbols.exampleComponent
}


/**
 * The base class for all aspects for the [[simx.applications.basicexamples.component.ExampleComponent]]
 *
 * @note The component type is defined in the process of inheriting from the [[simx.core.entity.description.EntityAspect]]
 *       class wherefore classes inheriting from this class
 * @author Dennis Wiebusch
 * @param aspectType the type of this aspect
 * @param targets the target components
 */
abstract class ExampleAspectBase( aspectType : GroundedSymbol, targets : List[Symbol] = Nil )
  extends EntityAspect(ExampleComponent.componentType, aspectType, targets)

/**
 * An sphere aspect for the [[simx.applications.basicexamples.component.ExampleComponent]]
 *
 * @author Dennis Wiebusch
 * @param id an id for this aspect
 */
case class ExampleSphereAspect(id : Symbol) extends ExampleAspectBase(Symbols.sphere){
  /**
   *
   * @return the features the entity will at least have when it is created
   */
  def getFeatures   = Set(Identifier, Transformation)

  /**
   *
   * @return a set of features
   */
  def getProvidings = Set(Identifier)

  /**
   *
   * @return a list of [[simx.core.entity.description.SVal]]'s containing the information needed to instanciate an
   *         entity with this aspect
   */
  def getCreateParams =
    new NamedSValSet(aspectType, Identifier(id))
}

/**
 * the converter for the local trafo svar
 */
object ExampleConverters{
  def registerConverters(){
    new Converter(LocalTrafo)(Transformation){
      /**
       * the actual conversion function
       * @param i the input data to be converted
       * @return the converted data
       */
      def convert(i: ConstVec3f) =
        ConstMat4f(Mat4x3f.translate(i))

      /**
       * the actual reversion function
       * @param i the input data to be reverted
       * @return the reverted data
       */
      def revert(i: Transformation.dataType) =
        ConstVec3f(i.m30, i.m31, i.m32)
    }
  }
}

/**
 *
 * The example component
 *
 * It stores identifiers of entities within a map and moves entities to exemplifiy the creation of and interaction
 * with entities
 *
 * @author Dennis Wiebusch
 * @param componentName the name of this component which may be used in the target list of aspects
 */
class ExampleComponent(val componentName : Symbol) extends Component{
  ExampleConverters.registerConverters()
  /**
   * The components type
   * @note We use identifier here as the component is thought to be responsible for the identifiers of entities.
   *       Normally one would use more reasonable values like graphics, physics, etc.
   * @return the components type
   */
  def componentType = ExampleComponent.componentType

  private var entitiesInCreation = Map[java.util.UUID, LocalEntityRep]()
  private var simulatedEntities  = Map[java.util.UUID, LocalEntityRep]()

  private case class LocalEntityRep(id : Identifier.dataType,
                                    var posSVar : Option[SVar[LocalTrafo.dataType]],
                                    var lastPos : LocalTrafo.dataType,
                                    var lastUpdateTime : Long)


  /**
   * Used to configure this component
   * @param params the configuration params
   */
  protected def configure(params: SValSet)   {}


  /**
   * Provided initial values for the entity to be created
   * @param toProvide the convertibletraits for which values shall be provided
   * @param aspect the aspect providing the context for this method call
   * @param e the entity to be filled
   * @param given a set of create parameters that were already provided
   *
   */
  protected def requestInitialValues(toProvide: Set[ConvertibleTrait[_]], aspect: EntityAspect, e: Entity, given: SValSet){
    val id    = aspect.getCreateParams.getFirstValueFor(Identifier)
    val trafo = given.getFirstValueFor(LocalTrafo)
    if (id.isDefined && trafo.isDefined)
      entitiesInCreation = entitiesInCreation.updated(e.id, LocalEntityRep(id.get, None, trafo.get,  -1))
    provideInitialValues(e, aspect.getCreateParams.combineWithValues(toProvide)._1)
  }

  /**
   *
   * @param e the entity to be integrated
   * @param aspect the aspect which the component has to process
   */
  protected def entityConfigComplete(e: Entity , aspect: EntityAspect) {
    // retrieve local representation and remove it from the stored list
    val toSimulate      = entitiesInCreation(e.id)
    entitiesInCreation  = entitiesInCreation - e.id
    // store trafo svar
    toSimulate.posSVar  = e.get(LocalTrafo).headOption
    // add local rep to
    simulatedEntities  += e.id -> toSimulate
    // access the identifier
    val id = aspect.getCreateParams.getFirstValueForOrElse(Identifier)(throw new Exception("Did not find id"))
    println("Example component got entity with id \"" + id.name + "\"")
  }

  /**
   * Perform one simulation step
   */
  protected def performSimulationStep() {
    val now = System.currentTimeMillis()
    simulatedEntities.values.foreach{ localRep =>
      if (localRep.lastUpdateTime < 0 )
        localRep.lastUpdateTime = now
      else {
        // time calculations
        val timeDiff            = (now - localRep.lastUpdateTime) / 16f
        localRep.lastUpdateTime = now
        // set new position
        localRep.lastPos        = (localRep.lastPos * Mat4x3f.rotateZ(functions.radians(timeDiff))).xyz
        localRep.posSVar.collect{ case svar => svar.set( localRep.lastPos ) }
      }
    }
    simulationCompleted()
  }

  /**
   * removes the given entity from the local representation
   * @param e the Entity to be removed
   */
  protected def removeFromLocalRep(e: Entity) {
    entitiesInCreation = entitiesInCreation - e.id
    simulatedEntities  = simulatedEntities  - e.id
  }
}
