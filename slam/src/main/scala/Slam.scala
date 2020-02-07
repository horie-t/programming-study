import scalafx.application.JFXApp
import scalafx.application.JFXApp.PrimaryStage
import scalafx.scene.canvas.Canvas
import scalafx.scene.Scene
import scalafx.scene.paint.Color

import scala.io.Source

class Scan2D(val sid: Int, val laserPoints: Seq[LaserPoint2D], val pose: Pose2D)

class LaserPoint2D(val sid: Int, val x: Double, val y: Double) {

}

object LaserPoint2D {
  val maxDistance = 6.0
  val minDistance = 0.1

  /**
    *
    * @param sid
    * @param distance
    * @param angle 単位は度(°)
    * @return
    */
  def calcPolar(sid: Int, distance: Double, angle: Double): Option[LaserPoint2D] = {
    if (minDistance <= distance && distance <= maxDistance) {
      val angleRad = math.toRadians(angle)
      Some(new LaserPoint2D(sid, distance * math.cos(angleRad), distance * math.sin(angleRad)))
    } else {
      None
    }
  }
}

/**
  * ロボットの姿勢データ
  * @param x
  * @param y
  * @param angle 単位は度(°)
  */
class Pose2D(val x: Double, val y: Double, val angle: Double) {
  val angleRad = math.toRadians(angle)
  val mat = Array.ofDim[Double](2, 2)
  mat(0)(0) = math.cos(angleRad)
  mat(1)(1) = math.cos(angleRad)
  mat(1)(0) = math.sin(angleRad)
  mat(0)(1) = - mat(1)(0)
}

object Scan2D {
  def readFile(path: String): Seq[Scan2D] = {
    Source.fromFile(path).getLines().flatMap(line => {
      val fields = line.split(" ").toList
      if (fields.head == "LASERSCAN") {
        val sid :: timeSec :: timeNSec :: countScanPoint :: rest = fields.drop(1)
        val (scanPoints, pose) = rest.map(_.toDouble).splitAt(countScanPoint.toInt * 2) match {
          case (scanData, poseAndTime) => {
            (scanData.grouped(2), poseAndTime.take(3))
          }
        }
        val laserPoints = scanPoints.flatMap(point => LaserPoint2D.calcPolar(sid.toInt, point(1), point(0))).toList
        Some(new Scan2D(sid.toInt, laserPoints, new Pose2D(pose(0), pose(1), pose(2))))
      } else {
        None
      }
    })
  }.toList
}

object ScalaFXHelloCanvas extends JFXApp {
  override def main(args: Array[String]): Unit = {
    super.main(args)
  }

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