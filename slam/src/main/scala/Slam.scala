import scalafx.application.JFXApp
import scalafx.application.JFXApp.PrimaryStage
import scalafx.scene.canvas.Canvas
import scalafx.scene.Scene
import scalafx.scene.paint.Color

object Slam extends JFXApp {
  val canvas = new Canvas(800, 800)
  val gc = canvas.graphicsContext2D

  gc.stroke = Color.Black
  gc.lineWidth = 1
  gc.strokeLine(40, 40, 760, 760)

  stage = new PrimaryStage {
    title = "SLAM"
    scene = new Scene {
      content = canvas
    }
  }
}