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

package simx.applications.basicexamples.cluster

import simx.core.components.renderer.setup._
import simplex3d.math.floatx.{Vec3f, ConstMat4f, Mat4x3f}
import simx.core.component.Frequency

/**
 * A tiled display configuration used by [[simx.applications.basicexamples.cluster.ExampleClusterApplication]].
 *
 * @author Stephan Rehfeld
 */
object TiledDisplayConfiguration {

  /**
   * This method creates a tiled display configuration
   *
   * @param widthInPx The amount of pixel in x direction. Must be larger than 0.
   * @param heightInPx The amount of pixel in y direction. Must be larger than 0.
   * @param fullscreen An optional parameter that sets if the application runs in fullscreen mode. Default value is false.
   * @param dpi The dots per inch of the screen. Must be larger than 0.0.
   * @param xScreens The amount of screen in x direction. Must be larger than 0.
   * @param yScreens The amount of screens in y direction. Must be larger than 0.
   * @param frequency Synchronization scheme for display. Should be [[simx.core.component.Unbound]] or [[simx.core.component.Triggered]]
   * @return A display configuration for a tiled display with the given amount of screen in x and y direction.
   */
  def apply( widthInPx: Int, heightInPx: Int, fullscreen : Boolean = false, dpi : Double = 48.0, xScreens : Int, yScreens : Int, frequency : Frequency ) : DisplaySetupDesc = {

    require( xScreens > 0, "The parameter 'xScreens' must be at least 1!")
    require( yScreens > 0, "The parameter 'yScreens' must be at least 1!")
    require( widthInPx > 0, "The parameter 'widthInPx' must be larger than 0!" )
    require( heightInPx > 0, "The parameter 'heightInPx' must be larger than 0!" )
    require( dpi > 0.0, "The parameter 'dpi' must be larger than 0!" )

    val widthOfScreenInMeters = widthInPx / dpi * 0.0254

    val displaySetupDesc = new DisplaySetupDesc

    val screenWidth = widthOfScreenInMeters / xScreens
    val screenHeight = widthOfScreenInMeters * heightInPx / widthInPx

    val leftPosition =  -((xScreens - 1) * 0.5 * screenWidth)
    val upperPosition =  (yScreens - 1) * 0.5 * screenHeight


    for( x <- 0 until xScreens ) {
      for( y <- 0 until yScreens ) {
        val i = y * xScreens + x + 1
        val displayDesc = new DisplayDesc( Some( (640,480)), (screenWidth, screenHeight), ConstMat4f( Mat4x3f.translate( Vec3f( (leftPosition + x * screenWidth).asInstanceOf[Float] , (/*1.165f + */upperPosition - y * screenHeight).asInstanceOf[Float], -0.6f ) ) ), new CamDesc( 0, Eye.RightEye, Some( 0.0f ) ) )
        val displayDevice = new DisplayDevice( None, displayDesc :: Nil, LinkType.SingleDisplay, Some( Symbol( "node" + i ) ) )
        displaySetupDesc.addDevice( displayDevice, i )
        displaySetupDesc.setFrequency( i, frequency )
      }
    }

    displaySetupDesc
  }
}
