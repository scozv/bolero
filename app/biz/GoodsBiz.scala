package biz

import base.mongo
import models._
import play.api.libs.json._
import play.modules.reactivemongo.json._
import reactivemongo.api.DB

import scala.concurrent.{ExecutionContext, Future}

object GoodsBiz extends CanConnectDB {
  def ctx(db: DB) =
    mongo.ctx(db, base.mongo.collectionName.GOODS)

  def getAllGoods
  (db: DB, goodsId: Seq[String] = Seq(), goodsTypes: Seq[String] = Seq())
  (implicit ec: ExecutionContext): Future[Seq[CoreGoods]] = ???

  def getTuanGoodsList(db: DB)(implicit ec: ExecutionContext): Future[Seq[TuanGoods]] = ???

  def getTuanGoods(db: DB, goodsId: String)(implicit ec: ExecutionContext): Future[Seq[TuanGoods]] = ???

  def getGoods(db: DB, goodsId: String)(implicit ec: ExecutionContext) = ???

  def getPictureId(db: DB, goodsId: String)(implicit ec: ExecutionContext): Future[Seq[String]] = ???

  def getThumbsnailId(db: DB, goodsId: String)(implicit ec: ExecutionContext): Future[String] = ???

  def importProducts(db: DB, goodsLst: Seq[CoreGoods])(implicit ec: ExecutionContext) = {
    if (goodsLst.groupBy(x=>x._id).keys.toList.length == goodsLst.length) {
      // no _id duplicated
      base.mongo.bulkInsert(ctx(db), goodsLst)
    } else Future.successful(-1)
  }

  def getAllTypies(keywords: Seq[String]): Future[Map[String, String]] = ???
}
