package biz.rules

import models._
import models.interop.HTTPResponseError
import reactivemongo.api.DB

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.ExecutionContext.Implicits.global

object TuanRules {
  private type OrderOrResponse = Either[TuanOrder, HTTPResponseError]
  private type ValidateOrder =
  (TuanOrder, => DB) => Future[OrderOrResponse]

  private[biz] val allRules: Seq[ValidateOrder] = Seq(
    cartItemNotBeEmpty,
    orderPriceMatched,
    orderPriceGreaterThanZero,
    limitedAmountForSpecialOrder
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
  (order: TuanOrder, db: => DB, rules: Seq[ValidateOrder])
  (implicit ec: ExecutionContext): Future[OrderOrResponse] =
    pipeRules(Left(order), db, rules)

  private def cartItemNotBeEmpty
  (order: TuanOrder, db: => DB)
  (implicit ec: ExecutionContext): Future[OrderOrResponse] = ???

  private def orderPriceGreaterThanZero
  (order: TuanOrder, db: => DB)
  (implicit ec: ExecutionContext): Future[OrderOrResponse] = ???

  private def orderPriceMatched
  (order: TuanOrder, db: => DB)
  (implicit ec: ExecutionContext): Future[OrderOrResponse] = ???

  private def limitedAmountForSpecialOrder
  (order: TuanOrder, db: => DB)
  (implicit ec: ExecutionContext): Future[OrderOrResponse] = ???
}
