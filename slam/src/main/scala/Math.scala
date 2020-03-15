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

  def *(n: Double): Vec2 = Vec2(x * n, y * n)
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

  def normalize(): Vec2 = {
    val len = length()
    Vec2(x / len, y / len)
  }
}

object Vec2 {
  def apply(x: Double, y: Double): Vec2 = new Vec2(x, y)
}

class Vec3(val x: Double, val y: Double, val z: Double) {

  /**
   * 和を返します。
   * @param v2
   * @return
   */
  def +(v2: Vec3): Vec3 = Vec3(x + v2.x, y + v2.y, z + v2.z)

  /**
   * 差を返します。
   * @param v2
   * @return
   */
  def -(v2: Vec3): Vec3 = Vec3(x - v2.x, y - v2.y, z - v2.z)

  /**
   * 内積を返します。
   * @param v2
   * @return
   */
  def *(v2: Vec3): Double = x * v2.x + y * v2.y + z * v2.z

  def *(n: Double): Vec3 = Vec3(x * n, y * n, z * n)
  def /(n: Double): Vec3 = Vec3(x / n, y / n, z / n)

}

object Vec3 {
  def apply(x: Double, y: Double, z: Double): Vec3 = new Vec3(x, y, z)
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

class Mat3(m00: Double, m01: Double, m02: Double,
           m10: Double, m11: Double, m12: Double,
           m20: Double, m21: Double, m22: Double) {
  val mat = Array(Array(m00, m01, m02), Array(m10, m11, m12), Array(m20, m21, m22))

  def +(m: Mat3): Mat3 = Mat3(
    mat(0)(0) + m.mat(0)(0), mat(0)(1) + m.mat(0)(1), mat(0)(2) + m.mat(0)(2),
    mat(1)(0) + m.mat(1)(0), mat(1)(1) + m.mat(1)(1), mat(1)(2) + m.mat(1)(2),
    mat(2)(0) + m.mat(2)(0), mat(2)(1) + m.mat(2)(1), mat(2)(2) + m.mat(2)(2)
  )

  def *(m: Mat3): Mat3 = Mat3(
    mat(0)(0) * m.mat(0)(0) + mat(0)(1) * m.mat(1)(0) + mat(0)(2) * m.mat(2)(0),
    mat(0)(0) * m.mat(0)(1) + mat(0)(1) * m.mat(1)(1) + mat(0)(2) * m.mat(2)(1),
    mat(0)(0) * m.mat(0)(2) + mat(0)(2) * m.mat(1)(0) + mat(0)(2) * m.mat(2)(2),

    mat(1)(0) * m.mat(1)(0) + mat(1)(1) * m.mat(1)(0) + mat(1)(2) * m.mat(2)(0),
    mat(1)(0) * m.mat(1)(1) + mat(1)(1) * m.mat(1)(1) + mat(1)(2) * m.mat(2)(1),
    mat(1)(0) * m.mat(0)(2) + mat(1)(2) * m.mat(1)(0) + mat(1)(2) * m.mat(2)(2),

    mat(2)(0) * m.mat(1)(0) + mat(2)(1) * m.mat(1)(0) + mat(2)(2) * m.mat(2)(0),
    mat(2)(0) * m.mat(1)(1) + mat(2)(1) * m.mat(1)(1) + mat(2)(2) * m.mat(2)(1),
    mat(2)(0) * m.mat(0)(2) + mat(2)(2) * m.mat(1)(0) + mat(2)(2) * m.mat(2)(2)
  )

  def *(v: Vec3): Vec3 = Vec3(
    mat(0)(0) * v.x + mat(0)(1) * v.y + mat(0)(2) * v.z,
    mat(1)(0) * v.x + mat(1)(1) * v.y + mat(1)(2) * v.z,
    mat(2)(0) * v.x + mat(2)(1) * v.y + mat(2)(2) * v.z
  )

  def *(n: Double): Mat3 = Mat3(
    mat(0)(0) * n, mat(0)(1) * n, mat(0)(2) * n,
    mat(1)(0) * n, mat(1)(1) * n, mat(1)(2) * n,
    mat(2)(0) * n, mat(2)(1) * n, mat(2)(2) * n
  )
  def /(n: Double): Mat3 = Mat3(
    mat(0)(0) / n, mat(0)(1) / n, mat(0)(2) / n,
    mat(1)(0) / n, mat(1)(1) / n, mat(1)(2) / n,
    mat(2)(0) / n, mat(2)(1) / n, mat(2)(2) / n
  )

  def t: Mat3 = Mat3(mat(0)(0), mat(1)(0), mat(2)(0), mat(0)(1), mat(1)(1), mat(2)(1), mat(0)(2), mat(1)(2), mat(2)(2))
  
  def apply(row: Int, col: Int): Double = mat(row)(col)
}

object Mat3 {
  def apply(m00: Double, m01: Double, m02: Double, m10: Double, m11: Double, m12: Double, m20: Double, m21: Double, m22: Double): Mat3 =
    new Mat3(m00, m01, m02, m10, m11, m12, m20, m21, m22)

  def inv(m: Mat3): Mat3 = {
    val determinant = (m.mat(0)(0) * m.mat(1)(1) * m.mat(2)(2) + m.mat(0)(1) * m.mat(1)(2) * m.mat(2)(0) + m.mat(0)(2) * m.mat(1)(0) * m.mat(2)(1)
      - m.mat(0)(2) * m.mat(1)(1) * m.mat(2)(0) - m.mat(0)(1) * m.mat(1)(0) * m.mat(2)(2) - m.mat(0)(0) * m.mat(1)(2) * m.mat(2)(1))

    Mat3(
       m.mat(1)(1) * m.mat(2)(2) - m.mat(1)(2) * m.mat(2)(1), -m.mat(0)(1) * m.mat(2)(2) + m.mat(0)(2) * m.mat(2)(1),  m.mat(0)(1) * m.mat(1)(2) - m.mat(0)(2) * m.mat(1)(1),
      -m.mat(1)(0) * m.mat(2)(2) + m.mat(1)(2) * m.mat(2)(0),  m.mat(0)(0) * m.mat(2)(2) - m.mat(0)(2) * m.mat(2)(0), -m.mat(0)(0) * m.mat(1)(2) + m.mat(0)(2) * m.mat(1)(0),
       m.mat(1)(0) * m.mat(2)(1) - m.mat(1)(1) * m.mat(2)(0), -m.mat(0)(0) * m.mat(2)(1) + m.mat(0)(1) * m.mat(2)(0),  m.mat(0)(0) * m.mat(1)(1) - m.mat(0)(1) * m.mat(1)(0)
    ) / determinant
  }
}