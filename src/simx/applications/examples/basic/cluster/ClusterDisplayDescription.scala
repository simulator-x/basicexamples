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

package simx.applications.examples.basic.cluster

import simx.core.components.renderer.setup._
import simplex3d.math.floatx._
import scala.Some


/**
 * This object creates a display description for a clustered rendering setup. This setup contains two nodes called
 * front and back. This description is used by the [[simx.applications.examples.basic.cluster.ExampleClusterApplication]].
 *
 * @author Stephan Rehfeld
 * @author Dennis Wiebusch
 */
object ClusterDisplayDescription {

  /**
   * Creates a display setup description with two nodes that should fit for every desktop computer or laptop.
   *
   * @param widthInPx The amount of pixel in x direction. Must be larger than 0.
   * @param heightInPx The amount of pixel in y direction. Must be larger than 0.
   * @param fullscreen An optional parameter that sets if the application runs in fullscreen mode. Default value is false.
   * @param dpi The dots per inch of the screen. Must be larger than 0.0.
   * @return The display description with two windows on two different nodes.
   */
  def apply(widthInPx: Int, heightInPx: Int, fullscreen : Boolean = false, dpi : Double = 48.0 ) : DisplaySetupDesc = {
    require( widthInPx > 0, "The parameter 'widthInPx' must be larger than 0!" )
    require( heightInPx > 0, "The parameter 'heightInPx' must be larger than 0!" )
    require( dpi > 0.0, "The parameter 'dpi' must be larger than 0!" )

    val widthOfScreenInMeters = widthInPx / dpi * 0.0254
    val displayDesc1 = new DisplayDesc(
      if (fullscreen) None else Some( widthInPx -> heightInPx ),
      widthOfScreenInMeters -> widthOfScreenInMeters * heightInPx / widthInPx,
      ConstMat4f( Mat4x3f.translate( Vec3f( 0.0f, 0.0f, -0.6f ) ) ),
      new CamDesc( 0, Eye.RightEye )
    )

    val displayDesc2 = new DisplayDesc(
      if (fullscreen) None else Some( widthInPx -> heightInPx ),
      widthOfScreenInMeters -> widthOfScreenInMeters * heightInPx / widthInPx,
      ConstMat4f( Mat4x3f.rotateY( functions.radians( 180f ) ).translate( Vec3f( 0.0f, 0.0f, -0.61f ) ) ),
      new CamDesc( 0, Eye.RightEye )
    )

    val displayDevice1 = new DisplayDevice( None, displayDesc1 :: Nil, LinkType.SingleDisplay, Some( 'back ) )
    val displayDevice2 = new DisplayDevice( None, displayDesc2 :: Nil, LinkType.SingleDisplay, Some( 'front ) )

    val displaySetupDesc = new DisplaySetupDesc()

    displaySetupDesc.addDevice( displayDevice1, 0 )
    displaySetupDesc.addDevice( displayDevice2, 1 )

    displaySetupDesc
  }
}