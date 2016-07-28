package models

import base.{beijingTime, text, mongo}
import models.interop.{CanBeMasked, CanBeJsonfied}
import play.api.libs.json._
import com.roundeights.hasher.Implicits._
import scala.language.postfixOps

case class AuthToken(
  _id: String,
  sha1: String,
  userId: String,
  createdAt: String, expiredAt: String) extends CanBeMasked[AuthToken] {

  def asMasked: AuthToken = AuthToken(_id, "", userId, "", expiredAt)
}

object AuthToken extends CanBeJsonfied[AuthToken] {
  def uuid = text.random(128)
  def prepareWithUser(user: User): AuthToken = {
    val uuid = AuthToken.uuid
    AuthToken(
      uuid,
      base.text.sha1(uuid),
      user._id,
      beijingTime.nowAtSeconds,
      base.beijingTime.dayAtSeconds(2))
  }

  import play.api.libs.functional.syntax._

  implicit val writes = new OWrites[AuthToken] {
    def writes(auth2: AuthToken) = Json.obj(
      "_id" -> auth2._id,
      "sha1" -> auth2.sha1,
      "userId" -> auth2.userId,
      "createdAt" -> auth2.createdAt,
      "expiredAt" -> auth2.expiredAt
    )
  }

  implicit val reads: Reads[AuthToken] = (
    (__ \ "_id").read[String] and
      (__ \ "sha1").read[String] and
      (__ \ "userId").read[String] and
      (__ \ "createdAt").read[String] and
      (__ \ "expiredAt").read[String]
    ) (AuthToken.apply _)
}