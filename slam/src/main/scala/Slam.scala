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
          val laserPoints = scanPoints.flatMap(point =>
            LaserPoint2D.calcPolar(sid.toInt, point(1), point(0) + angleOffset)).toList
          Some(new Scan2D(sid.toInt, laserPoints, new Pose2D(Vec2(pose(0), pose(1)), pose(2))))
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
    Pose2D(point - pose2.point, normalizeAngle(angleRad - pose2.angleRad))
  }

  def +(pose2: Pose2D): Pose2D = {
    Pose2D(point + pose2.point, normalizeAngle(angleRad + pose2.angleRad))
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
  val currentScan = scans.head
  drawScan(currentScan)
  val referenceScan = new Scan2D(currentScan.sid,
    currentScan.laserPoints.map(currentScan.pose.calcGlobalPoint), currentScan.pose)
  animation(scans.tail, referenceScan, currentScan.pose)

  def animation(scans: Seq[Scan2D], referenceScanGlobal: Scan2D, lastScanPose: Pose2D): Unit = {
    if (scans.nonEmpty) {
      val task = new Task[Scan2D]() {
        override protected def call: Scan2D = {
          Thread.sleep(100)
          val currentScan = scans.head

          // オドメトリの差分から現在の位置を推定
          val oddMotion = currentScan.pose - lastScanPose
          val predicatePose = referenceScanGlobal.pose + oddMotion
          estimatePose(predicatePose, currentScan, referenceScanGlobal) match {
            case Some(estimatedPose) =>
              new Scan2D(currentScan.sid, currentScan.laserPoints, estimatedPose)
            case None => {
              new Scan2D(currentScan.sid, currentScan.laserPoints, predicatePose)
            }
          }
        }

        /**
         * 位置・姿勢推定
         * @param poseIni 初期推定値
         * @param currentScan
         * @param referenceScanGlobal
         * @return
         */
        def estimatePose(poseIni: Pose2D, currentScan: Scan2D, referenceScanGlobal: Scan2D): Option[Pose2D] = {
          val costThreshold = 1.0            // コスト閾値(これ以上のコストしか求められない場合は失敗)
          val costDiffThreshold = 0.000001   // 繰り返してもこれ以下ならコスト計算終了
          val repeatMax = 100                // 繰り返しの上限

          /**
           *
           * @param posePre 前回推定値
           * @param poseMin
           * @param costMin
           * @param count
           * @return
           */
          def itr(posePre: Pose2D, poseMin: Pose2D, costMin: Double, count: Int): Option[Pose2D] = {
            if (count >= repeatMax) {
              println("abort")
              // 振動対策(いくら繰り返しても収束しなかった)
              if (costMin < costThreshold) {
                Some(poseMin)  // 計算を打ち切り
              } else {
                None           // 見つからなかった
              }
            } else {
              // 前回推定値から地図座標系での今回スキャンのポイントを算出し
              // 前回と今回のスキャンのマッチング
              val matchPointTuples = currentScan.laserPoints.flatMap { curPoint =>
                val curPointGlobal = posePre.calcGlobalPoint(curPoint)
                val closestPoint = referenceScanGlobal.laserPoints.minBy(_.point.squared_distance(curPointGlobal.point))
                if (closestPoint.point.distance(curPointGlobal.point) < 0.2) {
                  Some((curPoint, closestPoint))
                } else {
                  None
                }
              }.toList

              val (optimisedPose, cost) = optimisePose(posePre, matchPointTuples)
              if (math.abs(cost - costMin) < costDiffThreshold) {
                if (cost < costThreshold) {
                  println("found match")
                  Some(optimisedPose)    // 見つかった
                } else {
                  println("found local")
                  None                   // 不適切な局所解に陥った。
                }
              } else {
                if (cost < costMin) {
                  itr(optimisedPose, optimisedPose, cost, count + 1)
                } else {
                  itr(optimisedPose, poseMin, costMin, count + 1)
                }
              }
            }
          }

          itr(poseIni, poseIni, Double.MaxValue, 0)
        }

        /**
         * マッチしたスキャンから位置を推定
         * @param poseIni
         * @param matchPointTuples　マッチした(現在スキャン点(ローカル), 参照点(グローバル))のシーケンス。
         * @return
         */
        def optimisePose(poseIni: Pose2D, matchPointTuples: Seq[(LaserPoint2D, LaserPoint2D)]): (Pose2D, Double) = {
          def calcCost(pose: Pose2D): Double = {
            matchPointTuples.map { tuple =>
              val (curPoint, refPoint) = tuple
              pose.calcGlobalPoint(curPoint).point.squared_distance(refPoint.point)
            }.sum * 10
          }

          val costDiffThreshold = 0.000001
          val diffDistance = 0.00001
          val diffAngle = 0.00001

          val stepCoEff = 0.00001

          def itr(posePrev: Pose2D, costPrev: Double, poseMin: Pose2D, costMin: Double): (Pose2D, Double) = {
            val dEtx = (calcCost(Pose2D(posePrev.point + Vec2(diffDistance, 0), posePrev.angleRad)) - costPrev) / diffDistance
            val dEty = (calcCost(Pose2D(posePrev.point + Vec2(0, diffDistance), posePrev.angleRad)) - costPrev) / diffDistance
            val dEth = (calcCost(Pose2D(posePrev.point, posePrev.angleRad + diffAngle)) - costPrev) / diffAngle

            val pose = Pose2D(posePrev.point + Vec2(-dEtx * stepCoEff, -dEty * stepCoEff), posePrev.angleRad -dEth * stepCoEff)
            val cost = calcCost(pose)
            if (math.abs(cost - costPrev) < costDiffThreshold) {
              (pose, cost)
            } else {
              if (cost < costMin) {
                itr(pose, cost, pose, cost)
              } else {
                itr(pose, cost, poseMin, costMin)
              }
            }
          }

          itr(poseIni, calcCost(poseIni), poseIni, Double.MaxValue)
        }
      }
      task.setOnSucceeded( _ => {
        val scan = task.getValue
        drawScan(scan)
        val scanPrev = scans.head
        animation(scans.tail, scan, scanPrev.pose)
      })
      new Thread(task).start()
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