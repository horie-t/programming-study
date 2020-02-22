import javafx.concurrent.Task
import scalafx.application.JFXApp
import scalafx.application.JFXApp.PrimaryStage
import scalafx.scene.canvas.Canvas
import scalafx.scene.Scene
import scalafx.scene.paint.Color

import scala.io.Source

/**
 * 2次元ベクトル
 * @param x
 * @param y
 */
class Vec2(val x: Double, val y: Double) {
  def +(v2: Vec2): Vec2 = Vec2(x + v2.x, y + v2.y)
  def -(v2: Vec2): Vec2 = Vec2(x - v2.x, y - v2.y)

  def *(v2: Vec2): Double = x * v2.x + y * v2.y

  def length(): Double = math.sqrt(x * x + y * y)
  def squared_length() = x * x + y * y

  def distance(v2: Vec2) = {
    val dx = v2.x - x
    val dy = v2.y - y
    math.sqrt(dx * dx + dy * dy)
  }
  def squared_distance(v2: Vec2) = {
    val dx = v2.x - x
    val dy = v2.y - y
    dx * dx + dy * dy
  }
}

object Vec2 {
  def apply(x: Double, y: Double): Vec2 = new Vec2(x, y)
}

/**
 * 2x2行列
 * @param m00
 * @param m01
 * @param m10
 * @param m11
 */
class Mat2(val m00: Double, val m01: Double, val m10: Double, val m11: Double) {
  val mat = Array(Array(m00, m01), Array(m10, m11))

  /**
   * ドット積
   * @param v
   * @return
   */
  def *(v: Vec2): Vec2 = Vec2(mat(0)(0) * v.x + mat(0)(1) * v.y, mat(1)(0) * v.x + mat(1)(1) * v.y)

  /**
   * 転置行列
   * @return
   */
  def t: Mat2 = Mat2(mat(0)(0), mat(1)(0), mat(0)(1), mat(1)(1))

  def apply(row: Int, col: Int): Double = mat(row)(col)
}

object Mat2 {
  def apply(m00: Double, m01: Double, m10: Double, m11: Double): Mat2 = new Mat2(m00, m01, m10, m11)
}

/**
 * ロボットの姿勢データ
 * @param point
 * @param angleRad 単位はラジアン
 */
class Pose2D(val point: Vec2, val angleRad: Double) {
  val mat = Mat2(
    math.cos(angleRad), -math.sin(angleRad),
    math.sin(angleRad), math.cos(angleRad)
  )

  def -(pose2: Pose2D): Pose2D = {
    Pose2D(pose2.mat.t * (point - pose2.point), normalizeAngle(angleRad - pose2.angleRad))
  }

  def +(pose2: Pose2D): Pose2D = {
    Pose2D(pose2.mat * (point + pose2.point), normalizeAngle(angleRad + pose2.angleRad))
  }

  def calcGlobalPoint(localPoint: LaserPoint2D): LaserPoint2D = {
    LaserPoint2D(localPoint.sid, mat * localPoint.point + point)
  }

  private def normalizeAngle(angle: Double): Double = {
    if (angle < -math.Pi) {
      angle + 2 * math.Pi
    } else if (angle >= math.Pi) {
      angle - 2 * math.Pi
    } else {
      angle
    }
  }
}

object Pose2D {
  def apply(point: Vec2, angleRad: Double): Pose2D = new Pose2D(point, angleRad)
}

/**
 * ポイントの測定データ
 * @param sid
 * @param point
 */
class LaserPoint2D(val sid: Int, val point: Vec2) {

}

object LaserPoint2D {
  val maxDistance = 6.0
  val minDistance = 0.1

  def apply(sid: Int, point: Vec2): LaserPoint2D = new LaserPoint2D(sid, point)

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
      Some(LaserPoint2D(sid, Vec2(distance * math.cos(angleRad), distance * math.sin(angleRad))))
    } else {
      None
    }
  }
}

/**
 * 1回のスキャンデータ
 * @param sid
 * @param laserPoints
 * @param pose
 */
class Scan2D(val sid: Int, val laserPoints: Seq[LaserPoint2D], val pose: Pose2D)

object Scan2D {
  /**
   * ファイルからスキャンデータを取り込みます。
   * @param path
   * @return
   */
  def readFile(path: String): Seq[Scan2D] = {
    val angleOffset = 180

    val scansRaw = Source.fromFile(path).getLines().flatMap(line => {
      val fields = line.split(" ").toList
      if (fields.head == "LASERSCAN") {
        val sid :: timeSec :: timeNSec :: countScanPoint :: rest = fields.drop(1)
        if (rest.length >= countScanPoint.toInt * 2 + 3) {
          val (scanPoints, pose) = rest.map(_.toDouble).splitAt(countScanPoint.toInt * 2) match {
            case (scanData, poseAndTime) => {
              (scanData.grouped(2), poseAndTime.take(3))
            }
          }
          val laserPoints = scanPoints.flatMap(point =>
            LaserPoint2D.calcPolar(sid.toInt, point(1), point(0) + angleOffset)).toList
          Some(new Scan2D(sid.toInt, laserPoints, Pose2D(Vec2(pose(0), pose(1)), pose(2))))
        } else {
          // 途中でデータが途切れている
          None
        }
      } else {
        // スキャンデータ以外の行
        None
      }
    }).toList

    val scanFirst = scansRaw.head
    // 読み込んだデータのscan idを振り直し、姿勢データ初期姿勢からの変化に変更する。
    scansRaw.zipWithIndex.map{case (scan, index) => new Scan2D(index, scan.laserPoints, scan.pose - scanFirst.pose)}
  }
}

object Slam extends JFXApp {
  override def main(args: Array[String]): Unit = {
    scans = Scan2D.readFile(args(0))
    super.main(args)
  }

  var scans: Seq[Scan2D] = _

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

  // 自己位置を描画
  def animation(scans: Seq[Scan2D]): Unit = {
    if (scans.nonEmpty) {
      val task = new Task[Scan2D]() {
        override protected def call: Scan2D = {
          Thread.sleep(100)
          scans.head
        }
      }
      task.setOnSucceeded( _ => {
        val scan = task.getValue
        drawScan(scan)
        animation(scans.tail)
      })

      new Thread(task).start()
    }
  }
  animation(scans)

  stage = new PrimaryStage {
    title = "SLAM"
    scene = new Scene {
      content = canvas
    }
  }

  def drawScan(scan: Scan2D): Unit = {
    if (scan.sid % 10 == 0) {
      val x = scan.pose.point.x
      val y = scan.pose.point.y
      val length = 0.4
      val mat = scan.pose.mat
      gc.strokeLine(x, y, x + length * mat(0, 0), y + length * mat(1, 0))
      gc.strokeLine(x, y, x - length * mat(1, 0), y + length * mat(0, 0))
    }
    for (point <- scan.laserPoints) {
      val globalPoint = scan.pose.calcGlobalPoint(point).point
      gc.strokeRect(globalPoint.x, globalPoint.y, 0.02, 0.02)
    }
  }
}