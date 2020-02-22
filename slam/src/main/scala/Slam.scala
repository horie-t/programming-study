import scalafx.application.JFXApp
import scalafx.application.JFXApp.PrimaryStage
import scalafx.scene.canvas.Canvas
import scalafx.scene.Scene
import scalafx.scene.paint.Color

object Slam extends JFXApp {
  val canvas = new Canvas(800, 500)
  val gc = canvas.graphicsContext2D

  // 左下が負の象限になるようにする。
  gc.scale(20, 20)
  gc.transform(1, 0, 0, -1, 7.51, 7.51)  // 0.01は線の半分(これを足さないと線がボケる)

  // 枠を描く
  gc.stroke = Color.Black
  gc.lineWidth = 0.02
  gc.strokeLine(-5, -15, 30, -15)  // 下
  gc.strokeLine(-5, -15, -5,   5)  // 左
  gc.strokeLine(-5,   5, 30,   5)  // 上
  gc.strokeLine(30, -15, 30,   5)  // 右

  // 目盛りを描く
  // x軸
  for (i <- 0 to 25 by 5) {
    gc.strokeLine(i, -15, i, -14.8)
  }
  // y軸
  for (i <- -10 to 0 by 5) {
    gc.strokeLine(-5, i, -4.8, i)
  }

  stage = new PrimaryStage {
    title = "SLAM"
    scene = new Scene {
      content = canvas
    }
  }
}