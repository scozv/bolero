package models.interop

/**
  * 将Model中的敏感数据清除，返回新的Model对象
  */
trait CanBeMasked[T] {
  def asMasked: T
}
