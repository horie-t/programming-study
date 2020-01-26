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

import Vec3._

class Ray(val A: Vec3, val B: Vec3) {
  def origin: Vec3 = A
  def direction: Vec3 = B
  def pointAtParameter(t: Float) = A + (t * B)
}

object Ray {
  def apply(A: Vec3, B: Vec3): Ray = new Ray(A, B)
}

case class HitRecord(t: Float, p: Vec3, normal: Vec3, material: Material)

trait Hittable {
  def hit(ray: Ray, tMin: Float, tMax: Float): Option[HitRecord]
}

object Hittable {
  import util._

  def hit(ray: Ray, tMin: Float, tMax: Float, hittables: Seq[Hittable]): Option[HitRecord] = {
    val hits = hittables.map(_.hit(ray, tMin, tMax)).flatten
    if (hits.isEmpty) {
      None
    } else {
      Some(hits.minBy(_.t))
    }
  }

  def randomInUitSphere(): Vec3 = {
    def randVecs: Stream[Vec3] = (2.0f * Vec3(Random.nextFloat(), Random.nextFloat(), Random.nextFloat())
      - Vec3(1.0f, 1.0f, 1.0f)) #:: randVecs
    randVecs.filter(_.squared_length >= 1.0f).head
  }

  def ramdomInUnitDisk(): Vec3 = {
    def randVecs: Stream[Vec3] = (2.0f * Vec3(Random.nextFloat(), Random.nextFloat(), 0.0f)
      - Vec3(1.0f, 1.0f, 0.0f)) #:: randVecs
    randVecs.filter(p => dot(p, p) >= 1.0f).head
  }
}

import Hittable._

class Sphere(val center: Vec3, val radius: Float, val material: Material) extends Hittable {
  override def hit(ray: Ray, tMin: Float, tMax: Float): Option[HitRecord] = {
    val oc = ray.origin - center
    val a = dot(ray.direction, ray.direction)
    val b = dot(oc, ray.direction)
    val c = dot(oc, oc) - radius * radius
    val discriminant = b * b - a * c
    if (discriminant > 0) {
      val t = (-b - math.sqrt(discriminant).toFloat) / a
      if (tMin < t && t < tMax) {
        val p = ray.pointAtParameter(t)
        val normal = (p - center) / radius
        Some(HitRecord(t, p, normal, material))
      } else {
        val t = (-b + math.sqrt(discriminant).toFloat) / a
        if (tMin < t && t < tMax) {
          val p = ray.pointAtParameter(t)
          val normal = (p - center) / radius
          Some(HitRecord(t, p, normal, material))
        } else {
          None
        }
      }
    } else {
      None
    }
  }
}

trait Material {
  def scatter(ray: Ray, hitRecord: HitRecord): Option[(Vec3, Ray)]

  def reflect(v: Vec3, n: Vec3): Vec3 = v - 2 * dot(v, n) * n

  def refract(v: Vec3, n: Vec3, niOverNt: Float): Option[Vec3] = {
    val uv = unitVector(v)
    val dt = dot(uv, n)
    val discriminant = 1.0f - niOverNt * niOverNt * (1 - dt * dt)
    if (discriminant > 0) {
      Some(niOverNt * (uv - n * dt) - n * math.sqrt(discriminant).toFloat)
    } else {
      None
    }
  }
}

class Lambertian(val albedo: Vec3) extends Material {
  override def scatter(ray: Ray, rec: HitRecord): Option[(Vec3, Ray)] = {
    val target = rec.p + rec.normal + randomInUitSphere()
    val scattered = Ray(rec.p, target - rec.p)
    Some((albedo, scattered))
  }
}

class Metal(val albedo: Vec3, val fuzz: Float) extends Material {
  override def scatter(ray: Ray, rec: HitRecord): Option[(Vec3, Ray)] = {
    val reflected = reflect(unitVector(ray.direction), rec.normal)
    val scattered = Ray(rec.p, reflected + fuzz * randomInUitSphere())
    if (dot(scattered.direction, rec.normal) > 0) {
      Some((albedo, scattered))
    } else {
      None
    }
  }
}

class Dielectric(val refIdx: Float) extends Material {
  import util._

  override def scatter(ray: Ray, rec: HitRecord): Option[(Vec3, Ray)] = {
    val attenuation = Vec3(1.0f, 1.0f, 1.0f)
    val (outwardNormal, niOverNt, cosine) = if (dot(ray.direction, rec.normal) > 0.0f) {
      (-rec.normal, refIdx, refIdx * dot(ray.direction, rec.normal) / ray.direction.length())
    } else {
      (rec.normal, 1.0f / refIdx, - dot(ray.direction, rec.normal) / ray.direction.length())
    }

    refract(ray.direction, outwardNormal, niOverNt) match {
      case Some(refracted) => {
        if (Random.nextFloat() < schlick(cosine, refIdx)) {
          Some(attenuation, Ray(rec.p, reflect(ray.direction, rec.normal)))
        } else {
          Some(attenuation, Ray(rec.p, refracted))
        }
      }
      case None => Some(attenuation, Ray(rec.p, reflect(ray.direction, rec.normal)))
    }
  }

  def schlick(cosine: Float, refIdx: Float): Float = {
    val r0 = (1.0f - refIdx) / (1.0f + refIdx)
    val r0sq = r0 * r0
    r0 + (1.0f - r0) * math.pow((1.0f - cosine), 5.0f).toFloat
  }
}

class Camera(val lookFrom: Vec3, val lookAt: Vec3, val vUp: Vec3, val vfov: Float, aspect: Float,
             val aperture: Float, val focusDist: Float) {
  val lensRadius = aperture / 2.0f
  val theta = vfov * math.Pi.toFloat / 180.0f
  val halfHeight = math.tan(theta / 2.0f).toFloat
  val halfWidth = aspect * halfHeight
  val origin = lookFrom
  val w = unitVector(lookFrom - lookAt)
  val u = unitVector(cross(vUp, w))
  val v = cross(w, u)
  val lowerLeftCorner = origin -
    halfWidth * focusDist * u -
    halfHeight * focusDist * v -
    focusDist * w
  val horizontal = 2 * halfWidth * focusDist * u
  val vertical = 2 * halfHeight * focusDist * v

  def getRay(s: Float, t: Float): Ray = {
    val rd = lensRadius * randomInUitSphere()
    val offset = u * rd.x + v * rd.y
    Ray(origin + offset, lowerLeftCorner + s * horizontal + t * vertical - origin - offset)
  }
}

object RayTracing extends App {
  import util._

  val nx = 200
  val ny = 100
  val ns = 100
  print(s"P3\n${nx} ${ny}\n255\n")

  val lookFrom = Vec3(3.0f, 3.0f, 2.0f)
  val lookAt   = Vec3(0.0f, 0.0f, -1.0f)
  val distToFocus = (lookFrom - lookAt).length()
  val aperture = 2.0f
  val cam = new Camera(lookFrom, lookAt, Vec3(0.0f, 1.0f, 0.0f),
    20.0f, nx.toFloat/ ny.toFloat, aperture, distToFocus)

  val R = math.cos(math.Pi.toFloat / 4.0f).toFloat
  val world = Seq(
    new Sphere(Vec3(0.0f, 0.0f, -1.0f), 0.5f, new Lambertian(Vec3(0.1f, 0.2f, 0.5f))),
    new Sphere(Vec3(0.0f, -100.5f, -1.0f), 100.0f, new Lambertian(Vec3(0.8f, 0.8f, 0.0f))),
    new Sphere(Vec3(1.0f, 0.0f, -1.0f), 0.5f, new Metal(Vec3(0.8f, 0.6f, 0.2f), 0.3f)),
    new Sphere(Vec3(-1.0f, 0.0f, -1.0f), 0.5f, new Dielectric(1.5f)),
    new Sphere(Vec3(-1.0f, 0.0f, -1.0f), -0.45f, new Dielectric(1.5f))
  )

  for (j <- (ny - 1) to 0 by -1;
       i <- 0 until nx) {
    val colTmp = Seq.fill(ns){
      val u = (i.toFloat + Random.nextFloat()) / nx.toFloat
      val v = (j.toFloat + Random.nextFloat()) / ny.toFloat
      val r = cam.getRay(u, v)
      color(r, world, 0)
    }.reduceLeft(_ + _) / ns
    val col = Vec3(math.sqrt(colTmp(0)).toFloat, math.sqrt(colTmp(1)).toFloat, math.sqrt(colTmp(2)).toFloat)

    val ir = (255.99f * col(0)).toInt
    val ig = (255.99f * col(1)).toInt
    val ib = (255.99f * col(2)).toInt
    print(s"${ir} ${ig} ${ib}\n")
  }

  def color(r: Ray, hittables: Seq[Hittable], depth: Int): Vec3 = {
    Hittable.hit(r, 0.001f, Float.MaxValue, hittables) match {
      case Some(rec) => {
        rec.material.scatter(r, rec) match {
          case Some((attenuation, scatterd)) if (depth < 50) => {
            attenuation * color(scatterd, hittables, depth + 1)
          }
          case _ => Vec3(0.0f, 0.0f, 0.0f)
        }
      }
      case None => {
        val unitDirection = unitVector(r.direction)
        val t = 0.5f * (unitDirection.y + 1.0f)
        (1.0f - t) * Vec3(1.0f, 1.0f, 1.0f) + t * Vec3(0.5f, 0.7f, 1.0f)
      }
    }
  }
}
