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

  def apply(e0: Float, e1: Float, e2: Float): Vec3 = new Vec3(e0, e1, e2)

  def dot(v1: Vec3, v2: Vec3): Float = v1(0) * v2(0) + v1(1) * v2(1) + v1(2) * v2(2)
  def cross(v1: Vec3, v2: Vec3): Vec3 =
    new Vec3(
      v1(1) * v2(2) - v1(2) * v2(1),
      v1(2) * v2(0) - v1(0) * v2(2),
      v1(0) * v2(1) - v1(1) * v2(0)
  )

  def unitVector(v: Vec3): Vec3 = v / v.length()
}

class Ray(val A: Vec3, val B: Vec3) {
  def origin(): Vec3 = A
  def direction(): Vec3 = B
  def pointAtParameter(t: Float) = A + (t * B)
}

object Ray {
  def apply(A: Vec3, B: Vec3): Ray = new Ray(A, B)
}

import Vec3._

object RayTracing extends App {
  val nx = 200
  val ny = 100
  print(s"P3\n${nx} ${ny}\n255\n")
  val lowerLeftCorner = Vec3(-2.0f, -1.0f, -1.0f)
  val horizontal = Vec3(4.0f, 0.0f, 0.0f)
  val vertical = Vec3(0.0f, 2.0f, 0.0f)
  val origin = Vec3(0.0f, 0.0f, 0.0f)
  for (j <- (ny - 1) to 0 by -1;
       i <- 0 until nx) {
    val u = i.toFloat / nx.toFloat
    val v = j.toFloat / ny.toFloat
    val ray = Ray(origin, lowerLeftCorner + (u * horizontal) + (v * vertical))
    val col = color(ray)
    val ir = (255.99f * col(0)).toInt
    val ig = (255.99f * col(1)).toInt
    val ib = (255.99f * col(2)).toInt
    print(s"${ir} ${ig} ${ib}\n")
  }

  def hitSphere(center: Vec3, radius: Float, r: Ray): Float = {
    val oc = r.origin() - center
    val a = dot(r.direction(), r.direction())
    val b = 2.0f * dot(oc, r.direction())
    val c = dot(oc, oc) - radius * radius
    val discriminant = b * b - 4.0f * a * c
    if (discriminant < 0) {
      -1.0f
    } else {
      (-b - math.sqrt(discriminant).toFloat) / (2.0f * a)
    }
  }

  def color(r: Ray): Vec3 = {
    val t = hitSphere(Vec3(0.0f, 0.0f, -1.0f), 0.5f, r)
    if (t > 0.0f) {
      val N = unitVector(r.pointAtParameter(t) - Vec3(0.0f, 0.0f, -1.0f))
      0.5f * Vec3(N.x + 1.0f, N.y + 1.0f, N.z + 1.0f)
    } else {
      val unitDirection = unitVector(r.direction())
      val t = 0.5f * (unitDirection.y + 1.0f)
      (1.0f - t) * Vec3(1.0f, 1.0f, 1.0f) + t * Vec3(0.5f, 0.7f, 1.0f)
    }
  }
}
