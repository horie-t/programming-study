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
