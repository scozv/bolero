package models.interop

import play.api.libs.json.Json

case class WeChatOpenId
(access_token: String, expires_in: Long, refresh_token: String,
  openid: String, scope: String /*, unionid: String*/)

object WeChatOpenId {
  implicit val jsonFormats = Json.format[WeChatOpenId]
}

case class WeChatUserInfo
(openid: String, nickname: String,
sex: Int, city: String
 /*, unionid: String = "" do not use this unless your verify the WeChat developer account*/
)

object WeChatUserInfo {
  implicit val jsonFormats = Json.format[WeChatUserInfo]
}

case class WeChatAccessToken(access_token: String, expires_in: Long)
object WeChatAccessToken {
  implicit val jsonFormats = Json.format[WeChatAccessToken]
}