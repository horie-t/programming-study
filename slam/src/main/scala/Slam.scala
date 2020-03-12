import javafx.concurrent.Task
import scalafx.application.JFXApp
import scalafx.application.JFXApp.PrimaryStage
import scalafx.scene.canvas.Canvas
import scalafx.scene.Scene
import scalafx.scene.paint.Color

import scala.collection.mutable.ArrayBuffer
import scala.io.Source

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
    localPoint match {
      case linePoint: LaserPoint2DLine => LaserPoint2DLine(linePoint.sid, mat * linePoint.point + point, mat * linePoint.normal)
      case _ => LaserPoint2D(localPoint.sid, mat * localPoint.point + point)
    }
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
 * 直線上のスキャン点
 * @param sid スキャンID
 * @param point 位置座標
 * @param normal 法線ベクトル
 */
class LaserPoint2DLine(override val sid: Int, override val point: Vec2, val normal: Vec2) extends LaserPoint2D(sid, point) {
  def this(laserPoint2D: LaserPoint2D, normal: Vec2) {
    this(laserPoint2D.sid, laserPoint2D.point, normal)
  }
}

object LaserPoint2DLine {
  def apply(sid: Int, point: Vec2, normal: Vec2): LaserPoint2DLine = new LaserPoint2DLine(sid, point, normal)
  def apply(laserPoint2D: LaserPoint2D, normal: Vec2): LaserPoint2DLine = new LaserPoint2DLine(laserPoint2D, normal)
}

/**
 * コーナ上のスキャン点
 * @param sid スキャンID
 * @param point 位置座標
 * @param normal 法線ベクトル
 */
class LaserPoint2DCorner(override val sid: Int, override val point: Vec2, val normal: Vec2) extends LaserPoint2D(sid, point) {
  def this(laserPoint2D: LaserPoint2D, normal: Vec2) {
    this(laserPoint2D.sid, laserPoint2D.point, normal)
  }
}

object LaserPoint2DCorner {
  def apply(sid: Int, point: Vec2, normal: Vec2): LaserPoint2DLine = new LaserPoint2DLine(sid, point, normal)
  def apply(laserPoint2D: LaserPoint2D, normal: Vec2): LaserPoint2DLine = new LaserPoint2DLine(laserPoint2D, normal)
}

/**
 * 孤立点
 * @param sid スキャンID
 * @param point 位置座標
 */
class LaserPoint2DIsolate(override val sid: Int, override val point: Vec2) extends LaserPoint2D(sid, point) {

}

object LaserPoint2DIsolate {
  def apply(sid: Int, point: Vec2): LaserPoint2DIsolate = new LaserPoint2DIsolate(sid, point)
  def apply(laserPoint2D: LaserPoint2D): LaserPoint2DIsolate = new LaserPoint2DIsolate(laserPoint2D.sid, laserPoint2D.point)
}

/**
 * 1回のスキャンデータ
 * @param sid
 * @param laserPoints センサの測定値の並び。ローカル座標と地図座標の2パターンがある。
 * @param pose ロボットの位置ベクトル
 */
class Scan2D(val sid: Int, val laserPoints: Seq[LaserPoint2D], val pose: Pose2D) {
  /**
   * スキャン点の間隔を一定になるように再サンプリングします。
   * @return
   */
  def resamplePoints(): Scan2D = {
    /**
     * 補間点を見つけます。
     * @param currentPoint
     * @param prevPoint
     * @param accumulateDist
     * @return
     */
    def findInterpolatePoint(currentPoint: LaserPoint2D, prevPoint: LaserPoint2D, accumulateDist: Double): Either[Double, (LaserPoint2D, Boolean)] = {
      val dThresholdS = 0.05
      val dThresholdL = 0.25

      val diffVec = currentPoint.point - prevPoint.point
      val distance = diffVec.length()
      if (accumulateDist + distance < dThresholdS) {
        Left(accumulateDist + distance)
      } else if (accumulateDist + distance < dThresholdL) {
        val ratio = (dThresholdS - accumulateDist) / distance
        val point = diffVec * ratio + prevPoint.point
        Right((LaserPoint2D(currentPoint.sid, point), true))
      } else {
        Right((currentPoint, false))
      }
    }

    def itr(orgPoints: Seq[LaserPoint2D], prevPoint: LaserPoint2D, accumulateDist: Double, resamplePoints: List[LaserPoint2D]): Scan2D = {
      if (orgPoints.isEmpty) {
        new Scan2D(sid, resamplePoints.reverse, pose)
      } else {
        findInterpolatePoint(orgPoints.head, prevPoint, accumulateDist) match {
          case Left(dist) => {
            itr(orgPoints.tail, orgPoints.head, dist, resamplePoints)
          }
          case Right((lPoint, inserted)) => {
            itr(if (inserted) orgPoints else orgPoints.tail, lPoint, 0.0, lPoint :: resamplePoints)
          }
        }
      }
    }

    itr(laserPoints.tail, laserPoints.head, 0.0, laserPoints.head :: Nil)
  }

  /**
   * スキャン点を色線、コーナ、孤立に分類し、法線ベクトルを求める。
   * @return
   */
  def analysePoints(): Scan2D = {
    val fpdMin = 0.06    // 隣接点との最小距離
    val fpdMax = 1.0     // 隣接点との最大距離

    val cornerThreshold = 45.0 // これより角度が大きいとコーナとみなす。
    val cornerCosThreshold = math.cos(math.toRadians(cornerThreshold))

    // 法線ベクトルを計算
    def calcNormal(point: LaserPoint2D, adjacentPoints: Seq[LaserPoint2D]): Option[Vec2] = {
      adjacentPoints.dropWhile(_.point.distance(point.point) < fpdMin).headOption.flatMap { adjacentPoint =>
        if (adjacentPoint.point.distance(point.point) <= fpdMax) {
          val diff = adjacentPoint.point - point.point
          val normal = Vec2(diff.x, -diff.y).normalize()
          Some(normal)
        } else {
          None
        }
      }
    }

    def itr(leftPoints: List[LaserPoint2D], currentPoint: LaserPoint2D, rightPoints: Seq[LaserPoint2D]): Scan2D = {
      if (rightPoints.isEmpty) {
        new Scan2D(sid, leftPoints.reverse, pose)
      } else {
        val analysedPoint = (calcNormal(currentPoint, leftPoints), calcNormal(currentPoint, rightPoints)) match {
          case (Some(leftNormal), Some(rightNormal)) => {
            val rightNormalRev = rightNormal * -1
            val averageNormal = (leftNormal + rightNormalRev).normalize()
            if (math.abs(leftNormal * rightNormalRev) >= cornerCosThreshold) {
              LaserPoint2DLine(currentPoint, averageNormal)
            } else {
              LaserPoint2DCorner(currentPoint, averageNormal)
            }
          }
          case (Some(leftNormal), None) => {
            LaserPoint2DLine(currentPoint, leftNormal)
          }
          case (None, Some(rightNormal)) => {
            LaserPoint2DLine(currentPoint, rightNormal)
          }
          case (None, None) => {
            LaserPoint2DIsolate(currentPoint)
          }
        }

        itr(analysedPoint :: leftPoints, rightPoints.head, rightPoints.tail)
      }
    }

    itr(Nil, laserPoints.head, laserPoints.tail)
  }

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
        println("not match")
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
        refPoint match {
          case line: LaserPoint2DLine => {
            val curPointGlobal = pose.calcGlobalPoint(curPoint)
            val perpendicularDistance = (curPointGlobal.point - line.point) * line.normal
            perpendicularDistance * perpendicularDistance
          }
          case ref => pose.calcGlobalPoint(curPoint).point.squared_distance(ref.point)
        }
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

  def fusePose(referenceScanGlobal: Scan2D, estimatedPose: Pose2D, odoMotion: Pose2D, lastPose: Pose2D): Pose2D = {
    val matchPointTuples = laserPoints.flatMap { curPoint =>
      val curPointGlobal = estimatedPose.calcGlobalPoint(curPoint)
      val closestPoint = referenceScanGlobal.laserPoints.minBy(_.point.squared_distance(curPointGlobal.point))
      if (closestPoint.point.distance(curPointGlobal.point) < 0.2) {
        Some((curPoint, closestPoint))
      } else {
        None
      }
    }.toList

    estimatedPose // TODO
  }

  def calcIcpCovariance(estimatedPose: Pose2D, matchPointTuples: Seq[(LaserPoint2D, LaserPoint2DLine)]): Mat3 = {
    import Mat3.inv

    def calcPerpendicularDistance(scanPoint: LaserPoint2D, refPoint: LaserPoint2DLine, pose2D: Pose2D): Double = {
      val scanPointGlobal = pose2D.calcGlobalPoint(scanPoint)
      scanPointGlobal.point * refPoint.point
    }

    val diffDistance = 0.00001
    val diffAngle = math.toRadians(0.00001)

    // ヤコビ行列を計算
    val jacobianMat = matchPointTuples.map { tuple =>
      val (scanPoint, refPoint) = tuple

      val pd0 = calcPerpendicularDistance(scanPoint, refPoint, estimatedPose)
      val pdx = calcPerpendicularDistance(scanPoint, refPoint,
        Pose2D(Vec2(estimatedPose.point.x + diffDistance, estimatedPose.point.y), estimatedPose.angleRad))
      val pdy = calcPerpendicularDistance(scanPoint, refPoint,
        Pose2D(Vec2(estimatedPose.point.x, estimatedPose.point.y + diffDistance), estimatedPose.angleRad))
      val pdt = calcPerpendicularDistance(scanPoint, refPoint,
        Pose2D(Vec2(estimatedPose.point.x, estimatedPose.point.y), estimatedPose.angleRad + diffAngle))
      Vec3((pdx - pd0) / diffDistance, (pdy - pd0) / diffDistance, (pdt - pd0) / diffAngle)
    }

    // ヘッセ行列を、ガウス-ニュートン近似で計算
    val hessianMat = jacobianMat.foldLeft(Mat3(0, 0, 0, 0, 0, 0, 0, 0, 0)) { (hesMat, jacobRow) =>
      hesMat(0, 0) += jacobRow.x * jacobRow.x
      hesMat(0, 1) += jacobRow.x * jacobRow.y
      hesMat(0, 2) += jacobRow.x * jacobRow.z
      hesMat(1, 1) += jacobRow.y * jacobRow.y
      hesMat(1, 2) += jacobRow.y * jacobRow.z
      hesMat(2, 2) += jacobRow.z + jacobRow.z
      hesMat
    }
    hessianMat(1, 0) += hessianMat(0, 1)
    hessianMat(2, 0) += hessianMat(0, 2)
    hessianMat(2, 1) += hessianMat(1, 2)

    // 共分散行列は、ヘッセ行列の逆行列の定数倍とする(ラプラス近似)
    inv(hessianMat) * 0.1
  }

  def calcMotionCovariance(motion: Pose2D, diffTime: Double): Mat3 = {
    val transitionVelocity = motion.point.length() / diffTime
    val angularVelocity = motion.angleRad / diffTime

    val transitionVelocityThreshold = 0.02
    val angularVelocityThreshold = 0.05
    val (vt, wt) =
      (if (transitionVelocity < transitionVelocityThreshold) transitionVelocityThreshold else transitionVelocity,
       if (angularVelocity < angularVelocityThreshold) angularVelocityThreshold else angularVelocity)
    
    Mat3(0.001 * vt * vt,              0,              0,
                       0, 0.005* vt * vt,              0,
                       0,              0, 0.05 * wt * wt)
  }

  def rotateCovariance(pose: Pose2D, covariance: Mat3): Mat3 = {
    val cosign = Math.cos(pose.angleRad)
    val sign  = Math.sin(pose.angleRad)

    val jacobianRotate = Mat3(
      cosign, - sign, 0,
        sign, cosign, 0,
           0,      0, 1
    )

    jacobianRotate * covariance * jacobianRotate.t
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

  def calcRepresentativePoints(): Seq[LaserPoint2DLine] = {
    val representativePoints = (for (row <- grid;
                                     cell <- row)
      yield {
        val linePoints = cell.flatMap(_ match {
          case linePoint: LaserPoint2DLine => Some(linePoint)
          case _ => None
        })
        if (linePoints.isEmpty) {
          None
        } else {
          val (pointSum, normalSum) = linePoints.foldLeft((Vec2(0, 0), Vec2(0, 0))) { (vecPair, linePoint) =>
            (vecPair._1 + linePoint.point, vecPair._2 + linePoint.normal)
          }
          Some(LaserPoint2DLine(-1, pointSum / linePoints.length, normalSum / linePoints.length))
        }
      }).toList.flatten

    representativePoints
  }
}

object Slam extends JFXApp {
  override def main(args: Array[String]): Unit = {
    scans = Scan2D.readFile(args(0)).map(_.resamplePoints().analysePoints())
    super.main(args)
  }

  var scans: Seq[Scan2D] = _

  val canvas = new Canvas(800, 500)
//  val canvas = new Canvas(600, 600)
  val gc = canvas.graphicsContext2D

  // 左下が負の象限になるようにする。
  gc.scale(20, 20)
  gc.transform(1, 0, 0, -1, 7.51, 7.51)  // 0.01は線の半分(これを足さないと線がボケる)
//  gc.transform(1, 0, 0, -1, 12.51, 22.51)  // 0.01は線の半分(これを足さないと線がボケる)

  // 枠を描く
  gc.stroke = Color.Black
  gc.lineWidth = 0.02

  val left = -5
  val right = 30
  val bottom = -15
  val top = 5
//  val left = -10
//  val right = 15
//  val bottom = -5
//  val top = 20
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
//    title = "SLAM Corridor Perpendicular Distance"
    title = "SLAM Hall Interpolate Scan Point"
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