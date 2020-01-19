object RayTracing extends App {
  val nx = 200
  val ny = 100
  print(s"P3\n${nx} ${ny}\n255\n")
  for (j <- (ny - 1) to 0 by -1;
       i <- 0 until nx) {
    val r = i.toFloat / nx.toFloat
    val g = j.toFloat / ny.toFloat
    val b = 0.2.toFloat
    val ir = (255.99 * r).toInt
    val ig = (255.99 * g).toInt
    val ib = (255.99 * b).toInt
    print(s"${ir} ${ig} ${ib}\n")
  }
}
