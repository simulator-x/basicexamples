package simx.applications.examples.basic

/**
 * Created by IntelliJ IDEA.
 * User: martin
 * Date: 5/4/13
 * Time: 2:33 PM
 */

import simx.components.editor.EditorComponentAspect
import simx.core.{ApplicationConfig, SimXApplicationMain, SimXApplication}
import simx.components.renderer.jvr.{JVRComponentAspect, JVRInit}
import simx.core.component.ExecutionStrategy

import simx.core.ontology.{EntityDescription, types}
import simplex3d.math.float._
import collection.immutable
import simx.components.tuio.TuioComponentAspect
import simx.components.renderer.gui._

import simx.core.component.Soft

import simx.components.renderer.gui.objects.{Panel, Light}
import simx.core.worldinterface.eventhandling.{EventProvider, EventHandler}
import simx.components.renderer.gui
import simx.core.svaractor.{StateParticle, SVarActor}
import java.io.File
import simx.core.entity.Entity

import simx.components.renderer.gui.ontology.{types => lt}
import scala.util.Random
import simx.core.components.renderer.setup.BasicDisplayConfiguration
import simplex3d.math.floatx.ConstVec2f


/**
 * @author Martin Fischbach
 */
object GuiComponentExample extends SimXApplicationMain( new GuiComponentExample ) {
  val fullScreen = false
  //val useEditor = false
  val useEditor = askForOption("Use Editor Component?")
}

class GuiComponentExample extends SimXApplication with JVRInit with EventHandler with EventProvider {
  private val gfxName = 'renderer
  private val editorName = 'editor
  private val tuioName = 'tuio
  private val guiName = 'gui

  private val displaySetup = BasicDisplayConfiguration(1280, 800, fullscreen = GuiComponentExample.fullScreen)
  private var guiComponent: Option[SVarActor.Ref] = None

  private val screen = try {
    displaySetup.deviceGroups.head._2.dpys.head.displayDescs.head
  } catch {case _ : Throwable => throw new Exception("[GUI Component] At least one display is required.")}
  private val screenSize =  ConstVec2(screen.size._1.toFloat, screen.size._2.toFloat)

  override protected def applicationConfiguration =    ApplicationConfig withComponent
    JVRComponentAspect(gfxName, displaySetup) and
    TuioComponentAspect(tuioName, port = 3333) and
    GuiComponentAspect(guiName, displaySetup, 0.0001f) and
    EditorComponentAspect(editorName, "Gui Test") iff GuiComponentExample.useEditor

  protected def configureComponents(components: immutable.Map[Symbol, SVarActor.Ref]) {
    start(ExecutionStrategy where components(gfxName) runs Soft(60))
    exitOnClose(components(gfxName), shutdown)   
    guiComponent = Some(components(guiName))
    start(ExecutionStrategy where components(guiName) runs Soft(60))


//    handleDevice(types.Keyboard) {
//      (kb: Entity) =>
//        val beginPos = types.Position2D.withAnnotations(simx.core.ontology.Symbols.begin)(ConstVec2f(0.15f,-0.15f))
//        val endPos = types.Position2D.withAnnotations(simx.core.ontology.Symbols.end)(ConstVec2f(0.15f, 0.15f))
//        val duration = types.TimeInSeconds(3)
//        val size = types.Vector2(ConstVec2f(0.2f, 0.1f))
//        val text = types.String("Test")
//        val layer = types.Integer(3)
//        val textureFile = types.File("assets/vis/textures/backgroundAttributePanel.png")
//
//        kb.observe(types.Key_m).first {
//          pressed => if (pressed) {
//              LocalEvents.flyingNotification.emit(
//                beginPos,
//                endPos,
//                size,
//                duration,
//                text,
//                layer,
//                textureFile
//              )
//            }
//        }
//    }
  }

  private var textSVar: Option[StateParticle[String]] = None
  private var mainPanel: Option[Entity] = None
  private def nativeShapeOf(imgFile: File) = Panel.nativeShapeOf(displaySetup)(imgFile)

  protected def createEntities() {
    val sHalf = screenSize * 0.5f
    val base = new File("assets/vis")
    val buttonImage = new File(base, "textures/button-mid-res.png")

    val loadingScreen =
      gui.objects.ImagePanel(
        name = "Loading",
        imageFile = new File(base, "loading.jpg"),
        pos = Vec2.Zero,
        shape = QuadShape(screenSize),
        layer = 2)

    val panel1 =
      gui.objects.ImagePanel(
        name = "Background",
        imageFile = new File(base, "oldWood.jpg"),
        pos = Vec2(0f, 0f),
        shape = nativeShapeOf(new File(base, "oldWood.jpg")),
        layer = -2,
        //angle = 45f,
        subElements =
          gui.objects.TextPanel("BackgroundText", "Quest", Vec2.Zero, 0f, -1) ::
          gui.objects.Button("right", buttonImage, sHalf * Vec2.UnitX, nativeShapeOf(buttonImage)) ::
          gui.objects.Button("left", buttonImage, sHalf * -Vec2.UnitX, nativeShapeOf(buttonImage)) ::
          gui.objects.Button("top", buttonImage, sHalf * Vec2.UnitY, nativeShapeOf(buttonImage)) ::
          gui.objects.DragButton("bottom", buttonImage, sHalf * -Vec2.UnitY, nativeShapeOf(buttonImage)) :: Nil
      )

    val panel2 =
      gui.objects.DragButton("drag", buttonImage, (sHalf* 0.2f) * -Vec2.UnitX, QuadShape(Vec2.One * 0.08f), 0f, 1)

    val light =
      Light("the light", Vec3(0f, 0f, 10f))

    realizeWithLoadingScreen(
      loadingScreen,
      (light, handleEntityWithPrefix("[GuiTest] Background completed, result is:") _) ::
      (panel1, handleEntityWithPrefix("[GuiTest] MiniDragButton completed, result is:") _) ::
      (panel2, handleEntityWithPrefix("[GuiTest] Light completed, result is:") _) :: Nil
    )
  }

  private def handleEntityWithPrefix(p: String)(e: Entity) {println(p + " " + e)}


  protected def removeFromLocalRep(e : Entity){

  }

  protected def finishConfiguration() {
    Events.buttonPressed.observe(e => {
      val buttonName = e.get(types.Name).get
      textSVar.collect{case tSVar => tSVar.set(buttonName)}

      e.affectedEntities.foreach(affEnt => {
        if(buttonName == "Loot") affEnt.remove()
        ask[Provide2dWorldPositionOf](guiComponent.get, Ask2dWorldPositionOf(affEnt))( answer => {
          if(!Set("drag", "Loot").contains(buttonName)) {
            val color =
              ConstVec4(Random.nextFloat(), Random.nextFloat(), Random.nextFloat(), 0f)
            Events.explosion.emit(types.Position2D(answer.pos), lt.ShaderColor(color))
            val text = if(Random.nextBoolean()) "-1" else "Hello, this is dog!"
            Events.notification.emit(types.Position2D(answer.pos), types.String(text))
          }
          else if (buttonName == "drag"){
            val base = new File("assets/vis")
            val lootImage = new File(base, "textures/loot.png")
            for(i <- 0 until Random.nextInt(5)) {
              val lootDesc: EntityDescription =
                gui.objects.Button("Loot", lootImage, Vec2(answer.pos), nativeShapeOf(lootImage), isLoot = true)
              lootDesc.realize()
            }
          }
        })
      })
      println("[GuiTest] button pressed: " + e.get(types.Name))
    })

    onNextCreation(Symbol("BackgroundText") :: Nil)((guiElement: Entity) => {
      textSVar = guiElement.getSVars(simx.core.ontology.types.String).headOption.map(_._2)
    })

    onNextCreation(Symbol("Background") :: Nil)((guiElement: Entity) => {
      mainPanel = Some(guiElement)
    })

    handleDevice(types.Keyboard)( (keyboardEntity) => {
      val qSVar = keyboardEntity.getSVars(types.Key_q).head._2
      qSVar.observe( (pressed) => {if(pressed) sys.exit(0)} )
    })
    println("[Application is running]")
  }
}