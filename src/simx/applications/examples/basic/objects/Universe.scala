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

package simx.applications.examples.basic.objects

import simx.core.ontology.EntityDescription
import simx.core.components.physics.ImplicitEitherConversion._
import simx.core.components.physics.PhysSphere
import simplex3d.math.floatx.ConstVec3f

/**
 * TODO: Document Dennis
 *
 * @author Dennis Wiebusch
 */
case class Universe() extends EntityDescription(
    Ball("sun",   0.05f,  ConstVec3f(-.7f, 1f, -7f)),
    Ball("moon",  0.07f,  ConstVec3f(-.5f, 1f, -7f)),
    Ball("earth", 0.1f,   ConstVec3f(-.3f, 1f, -7f)) ,
    PhysSphere(mass = 0f, radius = 0.8f, transform = ConstVec3f(-.5f, -2f, -7f))
  )