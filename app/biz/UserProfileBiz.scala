package biz

import base.mongo
import base.mongo.userFields.IdentityType
import biz.interop.CanConnectDB
import models._
import reactivemongo.api.DB
import scala.concurrent.{ExecutionContext, Future}

object UserProfileBiz extends CanConnectDB {
  def ctx(db: DB) =
    mongo.ctx(db, base.mongo.collectionName.USERS)

  def addressCtx(db: DB) =
    base.mongo.ctx(db, base.mongo.collectionName.ADDRESS)

  def createUser
  (db: DB, user: User)
  (implicit write: play.api.libs.json.OWrites[models.User], ec: ExecutionContext): Future[User] = ???

  def updateUserWeChatInfo
  (db: DB, user: User)
  (implicit ec: ExecutionContext): Future[User] = ???

  /**
    * Gets user profile via ID of identityId, the identityId type is userId or WeChat OpenID ...
    * @return Some[User] if exists, otherwise None
    */
  def getProfile
    (db: DB, identityId: String, identityType: IdentityType.IdentityType = IdentityType.UserId)
    (implicit ec: ExecutionContext): Future[Option[User]] = ???

  /**
    * return true iff
    * 1) user is a valid user AND
    * 2) user do not create any order
    */
  def isFreshUser(db: DB, userId: String)(implicit ec: ExecutionContext): Future[Boolean] = {
    for {
      user <- getProfile(db, userId, mongo.userFields.IdentityType.UserId)
      orders <- OrderBiz.getOrders(db, userId)
    } yield user.isDefined && orders.isEmpty
  }
}
