class Vec3(e0: Float, e1: Float, e2: Float) {
  val e = Array(e0, e1, e2)

  def this() = {
    this(0, 0, 0)
  }

  def x(): Float = e(0)
  def y(): Float = e(1)
  def z(): Float = e(2)

  def r(): Float = e(0)
  def g(): Float = e(1)
  def b(): Float = e(2)

  def unary_+ : Vec3 = this
  def unary_- : Vec3 = new Vec3(-e(0), -e(1), -e(2))

  def apply(i: Int): Float = e(i)

  def +(v2: Vec3) = new Vec3(e(0) + v2(0), e(1) + v2(1), e(2) + v2(2))
  def -(v2: Vec3) = new Vec3(e(0) - v2(0), e(1) - v2(1), e(2) - v2(2))
  def *(v2: Vec3) = new Vec3(e(0) * v2(0), e(1) * v2(1), e(2) * v2(2))
  def /(v2: Vec3) = new Vec3(e(0) / v2(0), e(1) / v2(1), e(2) / v2(2))

  def *(t: Float) = new Vec3(t * e(0), t * e(1), t * e(2))
  def /(t: Float) = new Vec3(e(0) / t, e(1) / t, e(2) / t)

  def +=(v2: Vec3): Vec3 = {
    e(0) += v2(0)
    e(1) += v2(1)
    e(2) += v2(2)
    this
  }
  def -=(v2: Vec3): Vec3 = {
    e(0) -= v2(0)
    e(1) -= v2(1)
    e(2) -= v2(2)
    this
  }
  def *=(v2: Vec3): Vec3 = {
    e(0) *= v2(0)
    e(1) *= v2(1)
    e(2) *= v2(2)
    this
  }
  def *=(t: Float): Vec3 = {
    e(0) *= t
    e(1) *= t
    e(2) *= t
    this
  }
  def /=(v2: Vec3): Vec3 = {
    e(0) /= v2(0)
    e(1) /= v2(1)
    e(2) /= v2(2)
    this
  }
  def /=(t: Float): Vec3 = {
    e(0) /= t
    e(1) /= t
    e(2) /= t
    this
  }

  def length(): Float = scala.math.sqrt(e(0) * e(0) + e(1) * e(1) + e(2) * e(2)).toFloat
  def squared_length: Float = e(0) * e(0) + e(1) * e(1) + e(2) * e(2)
  def make_unit_vector(): Unit = {
    val k = 1.0f / length()
    e(0) *= k
    e(1) *= k
    e(2) *= k
  }
}

object Vec3 {
  implicit class FloatVec(self: Float) {
    def *(v: Vec3): Vec3 = new Vec3(self * v(0), self * v(1), self * v(2))
  }

  def dot(v1: Vec3, v2: Vec3): Float = v1(0) * v2(0) + v1(1) * v2(1) + v1(2) * v2(2)
  def cross(v1: Vec3, v2: Vec3): Vec3 =
    new Vec3(
      v1(1) * v2(2) - v1(2) * v2(1),
      v1(2) * v2(0) - v1(0) * v2(2),
      v1(0) * v2(1) - v1(1) * v2(0)
  )
}

object RayTracing extends App {
  val nx = 200
  val ny = 100
  print(s"P3\n${nx} ${ny}\n255\n")
  for (j <- (ny - 1) to 0 by -1;
       i <- 0 until nx) {
    val col = new Vec3(i.toFloat / nx.toFloat, j.toFloat / ny.toFloat, 0.2f)
    val ir = (255.99f * col(0)).toInt
    val ig = (255.99f * col(1)).toInt
    val ib = (255.99f * col(2)).toInt
    print(s"${ir} ${ig} ${ib}\n")
  }
}
