

case object LifeGame extends App {
  val blinker = Array(
    Array("□", "□", "□", "□", "□", "□"),
    Array("□", "□", "■", "□", "□", "□"),
    Array("□", "□", "■", "□", "□", "□"),
    Array("□", "□", "■", "□", "□", "□"),
    Array("□", "□", "□", "□", "□", "□"),
    Array("□", "□", "□", "□", "□", "□"),
  )
  val octagon = Array(
    Array("□", "□", "□", "■", "■", "□", "□", "□"),
    Array("□", "□", "■", "□", "□", "■", "□", "□"),
    Array("□", "■", "□", "□", "□", "□", "■", "□"),
    Array("■", "□", "□", "□", "□", "□", "□", "■"),
    Array("■", "□", "□", "□", "□", "□", "□", "■"),
    Array("□", "■", "□", "□", "□", "□", "■", "□"),
    Array("□", "□", "■", "□", "□", "■", "□", "□"),
    Array("□", "□", "□", "■", "■", "□", "□", "□"),
  )
  val spaceShip = Array(
    Array("□", "□", "□", "■", "■", "□", "□"),
    Array("□", "■", "□", "□", "□", "□", "■"),
    Array("■", "□", "□", "□", "□", "□", "□"),
    Array("■", "□", "□", "□", "□", "□", "■"),
    Array("■", "■", "■", "■", "■", "■", "□"),
  )
  var map = spaceShip

  def displayMap(map: Array[Array[String]]): Unit = {
    map.foreach(row => println(row.mkString))
  }

  def updateMap(map: Array[Array[String]]): Array[Array[String]] = {
    val newMap = Array.ofDim[String](map.length, map(0).length)
    for (y <- map.indices;
         x <- map(0).indices) {
      val count = (for (x1 <- (x - 1) to (x + 1);
                        y1 <- (y - 1) to (y + 1))
        yield {
          if (0 <= x1 && x1 < map(0).length && 0 <= y1 && y1 < map.length &&
            !(x1 == x && y1 == y) && map(y1)(x1) == "■") {
            1
          } else {
            0
          }
        }).sum
      if (map(y)(x) == "■" && (count == 2 || count == 3) ||
        map(y)(x) == "□" && count == 3) {
        newMap(y)(x) = "■"
      } else {
        newMap(y)(x) = "□"
      }
    }
    newMap
  }

  def play(): Unit = {
    displayMap(map)
    println("-" * 12)
    map = updateMap(map)
    Thread.sleep(500)
    play()
  }

  play()
}
