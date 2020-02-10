import javafx.concurrent.Task
import scalafx.application.JFXApp
import scalafx.application.JFXApp.PrimaryStage
import scalafx.animation.AnimationTimer
import scalafx.scene.canvas.Canvas
import scalafx.scene.Scene
import scalafx.scene.paint.Color

import scala.io.Source

/**
 * 1回のスキャンデータ
 * @param sid
 * @param laserPoints
 * @param pose
 */
class Scan2D(val sid: Int, val laserPoints: Seq[LaserPoint2D], val pose: Pose2D)

object Scan2D {
  val angleOffset = 180
  def readFile(path: String): Seq[Scan2D] = {
    Source.fromFile(path).getLines().flatMap(line => {
      val fields = line.split(" ").toList
      if (fields.head == "LASERSCAN") {
        val sid :: timeSec :: timeNSec :: countScanPoint :: rest = fields.drop(1)
        if (rest.length >= countScanPoint.toInt * 2 + 3) {
          val (scanPoints, pose) = rest.map(_.toDouble).splitAt(countScanPoint.toInt * 2) match {
            case (scanData, poseAndTime) => {
              (scanData.grouped(2), poseAndTime.take(3))
            }
          }
          val laserPoints = scanPoints.flatMap(point => LaserPoint2D.calcPolar(sid.toInt, point(1), point(0) + angleOffset)).toList
          Some(new Scan2D(sid.toInt, laserPoints, new Pose2D(pose(0), pose(1), pose(2))))
        } else {
          // 途中でデータが途切れている
          None
        }
      } else {
        // スキャンデータ以外の行
        None
      }
    }).toList
  }
}

/**
 * ポイントの測定データ
 * @param sid
 * @param x
 * @param y
 */
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
  val angleRad = angle
  val mat = Array.ofDim[Double](2, 2)
  mat(0)(0) = math.cos(angleRad)
  mat(1)(1) = math.cos(angleRad)
  mat(1)(0) = math.sin(angleRad)
  mat(0)(1) = - mat(1)(0)

  def calcGlobalPoint(localPoint: LaserPoint2D): LaserPoint2D = {
    val globalX = mat(0)(0) * localPoint.x + mat(0)(1) * localPoint.y + x
    val globalY = mat(1)(0) * localPoint.x + mat(1)(1) * localPoint.y + y
    new LaserPoint2D(localPoint.sid, globalX, globalY)
  }
}

object SlamScalaFx extends JFXApp {
  override def main(args: Array[String]): Unit = {
    scans = Scan2D.readFile(args(0))
    super.main(args)
  }

  var scans: Seq[Scan2D] = _
  val canvas = new Canvas(900, 600)
  val gc = canvas.graphicsContext2D

  // 左下が負の象限になるようにする。
  gc.scale(20, 20)
  gc.translate(32.5, 32.5)
  gc.transform(1, 0, 0, -1, 0, 0)

  // 枠を描く
  gc.stroke = Color.Black
  gc.lineWidth = 0.02
  gc.strokeLine(-30, 5, 10, 5)
  gc.strokeLine(-30, 5, -30, 30)
  gc.strokeLine(-30, 30, 10, 30)
  gc.strokeLine(10, 5, 10, 30)

  stage = new PrimaryStage {
    title = "SLAM ScalaFX"
    scene = new Scene {
      content = canvas
    }
  }

  def animation(scans: Seq[Scan2D], scanNum: Int): Unit = {
    if (scans.nonEmpty) {
      val task = new Task[Unit]() {
        override protected def call: Unit = {
          Thread.sleep(100)
        }

        override protected def succeeded(): Unit = {
          val scan = scans.head
          if (scanNum % 10 == 0) {
            val x = scan.pose.x
            val y = scan.pose.y
            val mat = scan.pose.mat
            val length = 0.4
            gc.strokeLine(x, y, x + length * mat(0)(0), y + length * mat(1)(0))
            gc.strokeLine(x, y, x - length * mat(1)(0), y + length * mat(0)(0))
          }
          for (point <- scan.laserPoints) {
            val globalPoint = scan.pose.calcGlobalPoint(point)
            gc.strokeRect(globalPoint.x, globalPoint.y, 0.02, 0.02)
          }
          animation(scans.tail, scanNum + 1)
        }
      }
      new Thread(task).start()
    }
  }
  animation(scans, 0)
}