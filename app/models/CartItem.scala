package models

import models.interop.{CanBeMasked, CanBeJsonfied}
import play.api.libs.json._
import play.api.libs.json.Reads._

trait CoreCartItem extends CanBeMasked[CoreCartItem] {
  val goods: CoreGoods
  val goodsAmount: Int
  val status: Int

  def isTuan = goods.isTuan
  def offsetAmount(offset: Int): CoreCartItem
  def asMasked: CoreCartItem
}

object CoreCartItem extends CanBeJsonfied[CoreCartItem] {
  import play.api.libs.functional.syntax._
  import play.api.libs.json._

  implicit val writes = new OWrites[CoreCartItem] {
    def writes(x: CoreCartItem) = Json.obj (
      "goods" -> x.goods,
      "goodsAmount" -> x.goodsAmount,
      "status" -> x.status
    )
  }

  implicit val reads: Reads[CoreCartItem] = (
    (__ \ "goods").read[models.CoreGoods] and
    (__ \ "goodsAmount").read[Int] and
    (__ \ "status").read[Int]
  )(CoreCartItem.apply _)

  def apply(goods: CoreGoods, goodsAmount: Int, status: Int) = goods match {
    case x: Goods => CartItem(x, goodsAmount, status)
    case x: TuanGoods => TuanCartItem(x, status)
  }
}

case class CartItem
(goods: Goods,
 goodsAmount: Int,
 status: Int) extends CoreCartItem {

  def offsetAmount(offset: Int) =
    CartItem(goods, Math.max(goodsAmount + offset, 0), status)

  def asMasked = CartItem(goods.asMasked, goodsAmount, status)
}

object CartItem extends CanBeJsonfied[CartItem] {
  implicit val reads: Reads[CartItem] = Json.reads[CartItem]
  val writes: OWrites[CartItem] = CoreCartItem.writes
}

case class TuanCartItem
(goods: TuanGoods,
 status: Int) extends CoreCartItem {

  val goodsAmount = 1
  def offsetAmount(offset: Int) = this
  def asMasked = TuanCartItem(goods.asMasked, status)
}

object TuanCartItem extends CanBeJsonfied[TuanCartItem] {
  implicit val reads: Reads[TuanCartItem] = Json.reads[TuanCartItem]
  val writes: OWrites[TuanCartItem] = CoreCartItem.writes
}