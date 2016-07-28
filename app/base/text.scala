package base

object text {
  def random(length: Int): String =
    scala.util.Random.alphanumeric.take(Math.min(Math.abs(length), 128)).mkString
  def randomAtSeconds(length: Int): String =
    base.beijingTime.nowAtSeconds.concat(random(length))

  def split(target: String, by: String = base.STRING_COMMA): Seq[String] =
    if (target.trim.isEmpty) Seq.empty
    else target.trim.split(by).filterNot(_.trim.isEmpty)

  import com.roundeights.hasher.Implicits._

  import scala.language.postfixOps
  def sha1(value: String, seven: Boolean = true): String = {
    val rs = value.sha1.hex
    if (seven) rs.substring(0, 7)
    else rs
  }
}
