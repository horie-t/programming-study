import javafx.concurrent.Task
import scalafx.application.JFXApp
import scalafx.application.JFXApp.PrimaryStage
import scalafx.scene.canvas.Canvas
import scalafx.scene.Scene
import scalafx.scene.paint.Color

import scala.collection.mutable.ArrayBuffer
import scala.io.Source

/**
 * 2次元ベクトル
 * @param x
 * @param y
 */
class Vec2(val x: Double, val y: Double) {
  /**
   * 和を返します。
   * @param v2
   * @return
   */
  def +(v2: Vec2): Vec2 = Vec2(x + v2.x, y + v2.y)

  /**
   * 差を返します。
   * @param v2
   * @return
   */
  def -(v2: Vec2): Vec2 = Vec2(x - v2.x, y - v2.y)

  /**
   * 内積を返します。
   * @param v2
   * @return
   */
  def *(v2: Vec2): Double = x * v2.x + y * v2.y

  def /(n: Double): Vec2 = Vec2(x / n, y / n)

  /**
   * 長さを返します。
   * @return
   */
  def length(): Double = math.sqrt(x * x + y * y)

  /**
   * 長さの自乗を返します。
   * @return
   */
  def squared_length() = x * x + y * y

  /**
   * 2点間の距離を返します。
   * @param v2
   * @return
   */
  def distance(v2: Vec2) = {
    val dx = v2.x - x
    val dy = v2.y - y
    math.sqrt(dx * dx + dy * dy)
  }

  /**
   * 2点間の距離の自乗を返します。
   * @param v2
   * @return
   */
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
 * ロボット位置ベクトル(2D座標と向きの角度の3次元ベクトル)
 * @param point 位置ベクトル
 * @param angleRad 向き(単位はラジアン)
 */
class Pose2D(val point: Vec2, val angleRad: Double) {
  val mat = Mat2(
    math.cos(angleRad), -math.sin(angleRad),
    math.sin(angleRad), math.cos(angleRad)
  )

  /**
   * 移動量を計算します。つまり、向きも含めた移動ベクトルを返す。
   * @param pose2 元の位置ベクトル
   * @return
   */
  def -(pose2: Pose2D): Pose2D = {
    Pose2D(pose2.mat.t * (point - pose2.point), normalizeAngle(angleRad - pose2.angleRad))
  }

  /**
   * ロボットを移動させます。
   * @param pose2 移動量
   * @return
   */
  def +(pose2: Pose2D): Pose2D = {
    Pose2D(point + mat * pose2.point, normalizeAngle(angleRad + pose2.angleRad))
  }

  /**
   * ロボットのローカル座標のスキャン・データから地図座標のポイントを算出します。
   * @param localPoint
   * @return
   */
  def calcGlobalPoint(localPoint: LaserPoint2D): LaserPoint2D = {
    LaserPoint2D(localPoint.sid, mat * localPoint.point + point)
  }

  /**
   * 角度を-π〜πの間に正規化します。
   * @param angle
   * @return
   */
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
 * @param sid スキャンID
 * @param point 位置座標
 */
class LaserPoint2D(val sid: Int, val point: Vec2) {

}

object LaserPoint2D {
  val maxDistance = 6.0
  val minDistance = 0.1

  def apply(sid: Int, point: Vec2): LaserPoint2D = new LaserPoint2D(sid, point)

  /**
   * 極座標系の測定値から直行座標に変換します。
   * @param sid スキャンID
   * @param distance センサからの距離
   * @param angle センサの向き(単位は度(°))
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
 * @param laserPoints センサの測定値の並び。ローカル座標と地図座標の2パターンがある。
 * @param pose ロボットの位置ベクトル
 */
class Scan2D(val sid: Int, val laserPoints: Seq[LaserPoint2D], val pose: Pose2D) {
  /**
   * スキャン・マッチングをします。
   * @param referenceScanGlobal 参照スキャン
   * @param lastScanPose 前回スキャン時のロボットの位置。
   * @return マッチングの結果。laserPointsは地図座標系になります。
   */
  def matchScan(referenceScanGlobal: Scan2D, lastScanPose: Pose2D): Scan2D = {
    val oddMotion = pose - lastScanPose
    val predicatePose = referenceScanGlobal.pose + oddMotion
    estimatePose(predicatePose, this, referenceScanGlobal) match {
      case Some(estimatedPose) =>
        new Scan2D(sid, laserPoints.map(estimatedPose.calcGlobalPoint), estimatedPose)
      case None => {
        new Scan2D(sid, laserPoints.map(predicatePose.calcGlobalPoint), predicatePose)
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
    /**
     * イテレータ
     * @param posePre 前回推定値
     * @param costPrev 前回のコスト
     * @param poseMin コスト最小のロボット推定位置
     * @param costMin 最小のコスト
     * @param count イテレーションの回数
     * @return 推定位置
     */
    def itr(posePre: Pose2D, costPrev: Double, poseMin: Pose2D, costMin: Double, count: Int): Option[Pose2D] = {
      val costThreshold = 1.0            // コスト閾値(これ以上のコストしか求められない場合は失敗)
      val costDiffThreshold = 0.000001   // 繰り返してもこれ以下ならコスト計算終了
      val repeatMax = 100                // 繰り返しの上限

      if (count >= repeatMax) {
        // 振動対策(いくら繰り返しても収束しなかった)
        if (costMin < costThreshold) {
          Some(poseMin)  // 計算を打ち切り
        } else {
          None           // 見つからなかった
        }
      } else {
        // 前回推定値から地図座標系での今回スキャンのポイントを算出し
        // 前回と今回のスキャンの測定点のマッチングする
        val matchPointTuples = currentScan.laserPoints.flatMap { curPoint =>
          val curPointGlobal = posePre.calcGlobalPoint(curPoint)
          val closestPoint = referenceScanGlobal.laserPoints.minBy(_.point.squared_distance(curPointGlobal.point))
          if (closestPoint.point.distance(curPointGlobal.point) < 0.2) {
            Some((curPoint, closestPoint))
          } else {
            None
          }
        }.toList

        // マッチングしたポイントの距離が最小になるロボット位置を最適化する。
        val (optimisedPose, cost) = optimisePose(posePre, matchPointTuples)
        if (math.abs(cost - costPrev) < costDiffThreshold) {
          if (cost < costThreshold && matchPointTuples.length > 50) {
            Some(optimisedPose)    // 見つかった
          } else {
            None                   // 不適切な局所解に陥った。
          }
        } else {
          if (cost < costMin) {
            itr(optimisedPose, cost, optimisedPose, cost, count + 1)
          } else {
            itr(optimisedPose, cost, poseMin, costMin, count + 1)
          }
        }
      }
    }

    itr(poseIni, Double.MaxValue, poseIni, Double.MaxValue, 0)
  }

  /**
   * マッチしたスキャンから位置を推定
   * @param poseIni
   * @param matchPointTuples　マッチした(現在スキャン点(ローカル), 参照点(グローバル))のシーケンス。
   * @return
   */
  def optimisePose(poseIni: Pose2D, matchPointTuples: Seq[(LaserPoint2D, LaserPoint2D)]): (Pose2D, Double) = {
    /**
     * ロボットの推定位置でのスキャン・マッチの誤差(コスト)を計算します。
     * @param pose ロボットの推定位置
     * @return
     */
    def calcCost(pose: Pose2D): Double = {
      matchPointTuples.map { tuple =>
        val (curPoint, refPoint) = tuple
        pose.calcGlobalPoint(curPoint).point.squared_distance(refPoint.point)
      }.sum / matchPointTuples.length * 100
    }

    /**
     * イテレータ
     * @param posePrev 前回推定位置
     * @param costPrev 前回のコスト
     * @param poseMin 最小のコストの推定位置
     * @param costMin 最小のコスト
     * @return
     */
    def itr(posePrev: Pose2D, costPrev: Double, poseMin: Pose2D, costMin: Double): (Pose2D, Double) = {
      /*
       * 最急降下法で位置を推定します。
       */
      val diffDistance = 0.00001
      val diffAngle = math.toRadians(0.00001)
      val stepCoEff = 0.00001
      val stepCoEffAngle = math.toRadians(0.00001)

      // 各方向の傾きを計算
      val dEtx = (calcCost(Pose2D(posePrev.point + Vec2(diffDistance, 0), posePrev.angleRad)) - costPrev) / diffDistance
      val dEty = (calcCost(Pose2D(posePrev.point + Vec2(0, diffDistance), posePrev.angleRad)) - costPrev) / diffDistance
      val dEth = (calcCost(Pose2D(posePrev.point, posePrev.angleRad + diffAngle)) - costPrev) / diffAngle

      // 下り方向へ移動してみて、その場所でのコストを計算
      val pose = Pose2D(posePrev.point + Vec2(-dEtx * stepCoEff, -dEty * stepCoEff), posePrev.angleRad - dEth * stepCoEffAngle)
      val cost = calcCost(pose)

      val costDiffThreshold = 0.000001
      if (math.abs(cost - costPrev) < costDiffThreshold) {
        // 前回との差が少なくなったので、計算終了
        (poseMin, costMin)
      } else {
        if (cost < costMin) {
          itr(pose, cost, pose, cost)
        } else {
          itr(pose, cost, poseMin, costMin)
        }
      }
    }

    val costIni = calcCost(poseIni)
    itr(poseIni, costIni, poseIni, costIni)
  }
}

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

class PointCloudMap {
  val cellSideSize = 0.05 // 一つのマスの辺の長さ(5cm)
  val gridSideSize = 80.0 // 格子の辺の長さ(80m)
  val cellSideCount = (gridSideSize / cellSideSize).toInt // 1辺のマスの数

  // 格子
  val grid = Array.fill[ArrayBuffer[LaserPoint2D]](cellSideCount, cellSideCount) { new ArrayBuffer[LaserPoint2D]()}

  /**
   * マスのインデックスを返します。
   *
   * @param x 位置
   * @return
   */
  private def calcCellIndex(x: Double): Int = {
    (x / cellSideSize).toInt + cellSideCount / 2
  }

  /**
   * マスのインデックスが格子の内側かどうかを返します。
   *
   * @param cellIndex
   * @return
   */
  private def isInGrid(cellIndex: Int): Boolean = {
    0 <= cellIndex && cellIndex < cellSideCount
  }

  def addPoints(pointGlobals: Seq[LaserPoint2D]): Unit = {
    // 測定点を格子に分割
    for (pointGlobal <- pointGlobals) {
      val (xIndex, yIndex) = (calcCellIndex(pointGlobal.point.x), calcCellIndex(pointGlobal.point.y))
      if (isInGrid(xIndex) && isInGrid(yIndex)) {
        grid(yIndex)(xIndex) += pointGlobal
      }
    }
  }

  def calcRepresentativePoints(): Seq[LaserPoint2D] = {
    val representativePoints = (for (row <- grid;
                                     cell <- row)
      yield {
        if (cell.isEmpty) {
          None
        } else {
          val pointAverage = (cell.map(_.point).reduce(_ + _)) / cell.length
          Some(LaserPoint2D(-1, pointAverage))
        }
      }).toList.flatten

    representativePoints
  }
}

object Slam extends JFXApp {
  override def main(args: Array[String]): Unit = {
    scans = Scan2D.readFile(args(0))
    super.main(args)
  }

  var scans: Seq[Scan2D] = _

//  val canvas = new Canvas(800, 500)
  val canvas = new Canvas(600, 600)
  val gc = canvas.graphicsContext2D

  // 左下が負の象限になるようにする。
  gc.scale(20, 20)
//  gc.transform(1, 0, 0, -1, 7.51, 7.51)  // 0.01は線の半分(これを足さないと線がボケる)
  gc.transform(1, 0, 0, -1, 12.51, 22.51)  // 0.01は線の半分(これを足さないと線がボケる)

  // 枠を描く
  gc.stroke = Color.Black
  gc.lineWidth = 0.02

//  val left = -5
//  val right = 30
//  val bottom = -15
//  val top = 5
  val left = -10
  val right = 15
  val bottom = -5
  val top = 20
  gc.strokeLine( left, bottom, right, bottom)  // 下
  gc.strokeLine( left, bottom,  left,    top)  // 左
  gc.strokeLine( left,    top, right,    top)  // 上
  gc.strokeLine(right, bottom, right,    top)  // 右

  // 目盛りを描く
  // x軸
  for (i <- left to right by 5) {
    gc.strokeLine(i, bottom, i, bottom + 0.2)
  }
  // y軸
  for (i <- bottom to top by 5) {
    gc.strokeLine(left, i, left + 0.2, i)
  }

  val pointCloudMap = new PointCloudMap()

  val currentScan = scans.head
  val referenceScan = new Scan2D(currentScan.sid,
    currentScan.laserPoints.map(currentScan.pose.calcGlobalPoint), currentScan.pose)
  drawScan(referenceScan)
  pointCloudMap.addPoints(currentScan.laserPoints.map(currentScan.pose.calcGlobalPoint))
  animation(scans.tail, currentScan.pose, currentScan.pose)

  // 地図を描画
  def animation(scans: Seq[Scan2D], lastPose: Pose2D, lastScanPose: Pose2D): Unit = {
    if (scans.nonEmpty) {
      val task = new Task[Scan2D]() {
        override protected def call: Scan2D = {
          Thread.sleep(100)
          val currentScan = scans.head
          val repPoints = pointCloudMap.calcRepresentativePoints()
          currentScan.matchScan(new Scan2D(currentScan.sid - 1, repPoints, lastPose), lastScanPose)
        }
      }
      task.setOnSucceeded( _ => {
        val scan = task.getValue
        drawScan(scan)
        val scanPrev = scans.head
        pointCloudMap.addPoints(scan.laserPoints)
        animation(scans.tail, scan.pose, scanPrev.pose)
      })

      new Thread(task).start()
    }
  }

  stage = new PrimaryStage {
    title = "SLAM Hall Grid Representative Point"
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
    for (laserPoint <- scan.laserPoints) {
      gc.strokeRect(laserPoint.point.x, laserPoint.point.y, 0.02, 0.02)
    }
  }
}