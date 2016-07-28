package biz.rules

import biz.{GoodsBiz}
import models._
import models.interop.HTTPResponseError
import reactivemongo.api.DB

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.ExecutionContext.Implicits.global

object OrderRules {
  private type OrderOrResponse = Either[CoreOrder, HTTPResponseError]
  private type ValidateOrder =
  (CoreOrder, => DB) => Future[OrderOrResponse]

  private[biz] val allRules: Seq[ValidateOrder] = Seq(
    cartItemNotBeEmpty,
    orderPriceGreaterThanZero,
    cartPriceLessThanCurrentGoods
  )

  private def pipeRules
  (order: OrderOrResponse, db: => DB, rules: Seq[ValidateOrder])
  (implicit ec: ExecutionContext): Future[OrderOrResponse] =
    rules.foldLeft(Future.successful(order)){ (acc, ruleItem) =>
      acc.flatMap { orderOrElse =>
        if (orderOrElse.isLeft) ruleItem(orderOrElse.left.get, db)
        else Future.successful(orderOrElse)
      }
    }

  private[biz] def pipeRules
  (order: CoreOrder, db: => DB, rules: Seq[ValidateOrder])
  (implicit ec: ExecutionContext): Future[OrderOrResponse] =
    pipeRules(Left(order), db, rules)

  /**
    * 购物车不能为空
    */
  private def cartItemNotBeEmpty
  (order: CoreOrder, db: => DB)
  (implicit ec: ExecutionContext): Future[OrderOrResponse] = {
    val rs =
      if (order.isEmpty) Right(HTTPResponseError.CART_EMPTY)
      else Left(order)

    Future.successful(rs)
  }

  /**
    * 订单的总价必须为正数
    */
  private def orderPriceGreaterThanZero
  (order: CoreOrder, db: => DB)
  (implicit ec: ExecutionContext): Future[OrderOrResponse] = {
    val rs =
      if (order.orderAmount <= 0.0) Right(HTTPResponseError.ORDER_PRICE_MUST_GRATER_THAN_ZERO)
      else Left(order)

    Future.successful(rs)
  }

    /**
    * 购物车的商品价格变动之后，我们需要动态地清理用户购物车
    * 保证订单过来的商品总价，不能Goods表中的商品现价
    */
  private def cartPriceLessThanCurrentGoods
  (order: CoreOrder, db: => DB)(implicit ec: ExecutionContext): Future[OrderOrResponse] = {

    val allGoods = GoodsBiz.getAllGoods(db, order.orderItems.map(_.goods._id))

    allGoods.map { goods =>
      val currentPrice = goods.foldLeft(0.0){ (acc, x) =>
        val itemInCart = order.orderItems.find(_.goods._id == x._id)
        val itemCurrentPrice =
          if (itemInCart.isDefined) itemInCart.get.goodsAmount * x.price
          else 0.0

        acc + itemCurrentPrice
      }

      // the price that user ordered must <= db updated price
      // cause if not, order may cancle this order, and re-reder with lower db updated price
      if (order.orderAmount > currentPrice) Right(HTTPResponseError.ORDER_PRICE_NOT_MATCHED)
      else Left(order)
    }
  }
}
