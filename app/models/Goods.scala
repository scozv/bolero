package models

import models.interop.{CanBeHierarchicInstance, CanBeHierarchicObject, CanBeJsonfied, CanBeMasked}
import play.api.libs.json._
import play.api.libs.json.Reads._

trait CoreGoods extends CanBeMasked[CoreGoods] {
  val _id: String
  val title: String
  val price: Double
  val cost: Double
  val goodsType: String

  /**
    * 是否为团购Goods
    */
  val isTuan: Boolean

  def asMasked: CoreGoods
}

object CoreGoods extends CanBeJsonfied[CoreGoods] with CanBeHierarchicObject {
  import play.api.libs.functional.syntax._

  implicit val writes: OWrites[CoreGoods] = new OWrites[CoreGoods] {
    def writes(x: CoreGoods) = x match {
      case x: Goods => coreWrites.writes(x)
      case x: TuanGoods => coreWrites.writes(x) ++ TuanGoods.writes.writes(x)
    }
  }

  private val coreWrites: OWrites[CoreGoods] = new OWrites[CoreGoods] {
    def writes(x: CoreGoods) = Json.obj (
      "_id" -> x._id,
      "title" -> x.title,
      "price" -> x.price,
      "cost" -> x.cost
    )
  }

  implicit val reads: Reads[CoreGoods] = (
    (__ \ "_id").read[String] and
      (__ \ "title").read[String] and
      (__ \ "price").read[Double] and
      (__ \ "cost").read[Double] and
      (__ \ "goodsType").read[String] and
      // 团购业务数据
      (__ \ rootFieldName).readNullable[String] and
      (__ \ "tuanMinPrice").readNullable[Double] and
      (__ \ "tuanDuration").readNullable[Int] and
      (__ \ "tuanShippingDuration").readNullable[Int]
    )(CoreGoods.apply _)

  def apply
  (_id: String,
   title: String,
   price: Double,
   cost: Double,
   goodsType: String,
   // 团购业务数据结构
   tuanRootGoodsId: Option[String],
   tuanMinPrice: Option[Double],
   tuanDuration: Option[Int],
   tuanShippingDuration: Option[Int]): CoreGoods = goodsType match {

    case base.STRING_TUAN => TuanGoods(
      _id,
      title,
      price,
      cost,
      tuanRootGoodsId.getOrElse(base.STRING_EMPTY),
      tuanMinPrice.get,
      tuanDuration.get,
      tuanShippingDuration.get)
    case _ => Goods(
      _id,
      title,
      price,
      cost)
  }
}

case class Goods
  (_id: String,
   title: String,
   price: Double,
   cost: Double) extends CoreGoods {

  val isTuan = false
  val goodsType = base.STRING_COMMA

  def asMasked: Goods =
    Goods(
      _id,
      title,
      price,
      price)
}

object Goods extends CanBeJsonfied[Goods] {
  implicit val reads: Reads[Goods] = Json.reads[Goods]
  val writes: OWrites[Goods] = CoreGoods.writes
}

case class TuanGoods
(_id: String,
 title: String,
 price: Double,
 cost: Double,
 rootId: String,
 tuanMinPrice: Double,
 tuanDuration: Int,
 tuanShippingDuration: Int) extends CoreGoods with CanBeHierarchicInstance  {

  val goodsType = base.STRING_TUAN
  val isTuan = true
  // val tuanRootOrderIdrootId = tuanRootGoodsId
  val isRoot = rootId.trim.isEmpty || rootId == _id

  def asMasked: TuanGoods =
    TuanGoods(
      _id,
      title,
      price,
      price,
      rootId,
      tuanMinPrice,
      tuanDuration,
      tuanShippingDuration)
}

object TuanGoods extends CanBeJsonfied[TuanGoods] {
  implicit val reads: Reads[TuanGoods] = Json.reads[TuanGoods]
  val writes: OWrites[TuanGoods] = new OWrites[TuanGoods] {
    def writes(x: TuanGoods) = Json.obj (
      base.mongo.generalFields.hierarchicId -> x.rootId.trim,
      "tuanMinPrice" -> x.tuanMinPrice,
      "tuanDuration" -> x.tuanDuration,
      "tuanShippingDuration" -> x.tuanShippingDuration
    )
  }
}