import scalafx.application.JFXApp
import scalafx.application.JFXApp.PrimaryStage
import scalafx.scene.canvas.Canvas
import scalafx.scene.Scene
import scalafx.scene.paint.Color

object ScalaFXHelloCanvas extends JFXApp {
  val canvas = new Canvas(700, 700)
  val gc = canvas.graphicsContext2D

  // 左下が負の象限になるようにする。
  gc.scale(50, 50)
  gc.translate(7, 7)
  gc.transform(1, 0, 0, -1, 0, 0)

  // 枠を描く
  gc.stroke = Color.Black
  gc.lineWidth = 0.02
  gc.strokeLine(-6, -6, 6, -6)
  gc.strokeLine(-6, -6, -6, 6)
  gc.strokeLine(-6, 6, 6, 6)
  gc.strokeLine(6, -6, 6, 6)

  stage = new PrimaryStage {
    title = "ScalaFX HelloCanvas"
    scene = new Scene {
      content = canvas
    }
  }
}