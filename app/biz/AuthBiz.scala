package biz

import base.mongo
import biz.interop.CanConnectDB
import models._
import models.interop.WeChatUserInfo
import reactivemongo.api.DB

import scala.concurrent.{ExecutionContext, Future}

object AuthBiz extends CanConnectDB {
  def ctx(db: DB) =
    mongo.ctx(db, base.mongo.collectionName.AUTHS)

  def userCtx(db: DB) =
    base.mongo.ctx(db, base.mongo.collectionName.USERS)

  def assignToken(db: DB, user: User)(implicit ec: ExecutionContext): Future[String] = ???

  def verifyToken(db: DB, user: User)(implicit  ec: ExecutionContext): Future[Option[String]] = ???

  def sha1(db: DB, userId: String)(implicit ec: ExecutionContext): Future[String] = ???

  def getUserId(db: DB, token: String)(implicit  ec: ExecutionContext): Future[Option[String]] = ???

  def authUserByWeChat
    (db: DB, userOrNone: Option[models.User], wechatUserInfo: WeChatUserInfo)
    (implicit  ec: ExecutionContext): Future[User] = ???




}