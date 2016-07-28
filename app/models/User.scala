package models

import base.{beijingTime, text, mongo}
import models.interop.{CanBeMasked, WeChatUserInfo, CanBeJsonfied}
import play.api.libs.json.{Format, Json}
import mongo.userFields.IdentityType
import mongo.userFields.IdentityType.IdentityType
import play.modules.reactivemongo.json._

case class User
  (_id: String,
   userName: String) extends CanBeMasked[User] {

  def asMasked: User =
    User(
      "masked_id",
      userName
    )
}

object User extends CanBeJsonfied[User] {
  def asWeChatUser(wechatUserInfo: models.interop.WeChatUserInfo): User =
    User(_id="", userName=wechatUserInfo.nickname)

  import play.api.libs.functional.syntax._
  import play.api.libs.json._

  implicit val writes = new OWrites[User] {
    def writes(x: User) = {
      val json = Json.obj(
        "_id" -> x._id,
        "userName" -> x.userName)

      json
    }
  }

  implicit val reads: Reads[User] = (
    (__ \ "_id").read[String] and
    (__ \ "userName").read[String]
  )(User.apply _)
}