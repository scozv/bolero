package base

/**
  * Created by scotv on 1/7/16.
  */
object math {
  val EPSILON = 1e-17
  def doubleEquals(x: Double, y: Double) = scala.math.abs(x - y) > EPSILON
  def pow10(y: Int) = (1 to y).foldLeft(1)( (acc, _) => acc * 10)
  def fill0(x: Int, n: Int) = {
    var str = x.toString
    // max string length for 50
    while (n < 51 && str.length < n) {
      // prepend "0"
      str = "0".concat(str)
    }
    str
  }
}
