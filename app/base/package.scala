import scala.concurrent.Future

package object base {
  val STRING_COMMA = ","
  val STRING_NIL = "-x0199x"
  val STRING_TUAN = "TUAN"
  val STRING_EMPTY = ""

  val ORDER_SHIPPING_FREE_THRESHOLD = 99.0
  val ORDER_PRICE_FOR_SHIPPING = 6.0

  def seqMerge[T]
  (a: Traversable[T], b: Traversable[T],
   key: T => String, mergeItem: (T, T) => T) = {

    val result = scala.collection.mutable.Set[T]()

    a.foreach { x =>
      b.find(key(_) == key(x)) match {
        case Some(y) => result.add(mergeItem(x, y))
        case None => result.add(x)
      }
    }

    // push elem of b that not exists in a
    b.foreach { y =>
      a.find(key(y) == key(_)) match {
        case None => result.add(y)
        case Some(x) => result
      }
    }

    result
  }

  def fs[T](x: T): Future[T] =
    Future.successful(x)
}
