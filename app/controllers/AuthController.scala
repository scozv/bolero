package controllers

import javax.inject.Inject
import models.interop._
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json._
import play.api.libs.ws._
import play.api.mvc.{Action, Controller}
import scala.concurrent.Future

// Reactive Mongo imports
import play.modules.reactivemongo.{MongoController, ReactiveMongoApi, ReactiveMongoComponents}

// BSON-JSON conversions/collection

class AuthController @Inject() (val ws: WSClient,  val reactiveMongoApi: ReactiveMongoApi)
  extends Controller
  with MongoController with ReactiveMongoComponents
  with CanCrossOrigin {

  val WECHAT_APPID = base.runtime.conf("bolero.inject.wechat.appid")
  val WECHAT_SECRET = base.runtime.conf("bolero.inject.wechat.secret")

  private type AuthOrResponse[T] = Either[T, HTTPResponseError]

  def applyAuth = Action.async(parse.json) { request =>
    /*
    String => Try[String]
    1. get identity string from client side, such as WeChat OpenId or else
    2. find account (user) according to WeChat OpenId in user table
       if not exists, we create a user for that WeChat OpenId, and apply a token in Auth table
    3. if exists the User, we try to find the token in Auth table via userId
       we may return valid token or TokenNotFoundException or TokenExpirationException
    */

    ???
  }

  private def wechatResponseValidate[T]
  (request: WSRequest, error: String => HTTPResponseError.WECHAT_ERROR)
  // TODO implicit is run-time parameter so that we dont care what it exactly is
  // TODO assuming it is ok, then we can invoke it
  (implicit rds: Reads[T]): Future[AuthOrResponse[T]] = {
    request.get.map(_.json).map { json =>
      val data = json.validate[T]

      if (data.isSuccess) Left(data.get)
      else Right(error(json.toString))
    }
  }

  private def requestOpenId(wechatClientCode: String): Future[AuthOrResponse[WeChatOpenId]] = {
    val wechatRequest = ws.url("https://api.weixin.qq.com/sns/oauth2/access_token")
      .withHeaders("Accept" -> "application/json")
      .withRequestTimeout(10000)
      .withQueryString(
        "appid" -> WECHAT_APPID,
        "secret" -> WECHAT_SECRET,
        "code" -> wechatClientCode,
        "grant_type" -> "authorization_code"
      )

    wechatResponseValidate[WeChatOpenId](
      wechatRequest, (x: String) => HTTPResponseError.WECHAT_OPENID_ERROR(x))
  }

  private def requestUserInfo(accessToken: String, wechatOpenId: String): Future[AuthOrResponse[WeChatUserInfo]] = {
    val wechatRequest = ws.url("https://api.weixin.qq.com/sns/userinfo")
      .withHeaders("Accept" -> "application/json")
      .withRequestTimeout(10000)
      .withQueryString(
        "access_token" -> accessToken,
        "openid" -> wechatOpenId,
        "lang" -> "zh_CN"
      )

    wechatResponseValidate[WeChatUserInfo](
      wechatRequest, (x: String) => HTTPResponseError.WECHAT_USERINFO_ERROR(x))
  }

  private def requestAccessToken: Future[AuthOrResponse[WeChatAccessToken]] = {
    val wechatRequest = ws.url("https://api.weixin.qq.com/cgi-bin/token")
      .withHeaders("Accept" -> "application/json")
      .withRequestTimeout(10000)
      .withQueryString(
        "grant_type" -> "client_credential",
        "appid" -> WECHAT_APPID,
        "secret" -> WECHAT_SECRET
      )

    wechatResponseValidate[WeChatAccessToken](
      wechatRequest, (x: String) => HTTPResponseError.WECHAT_ACCESSTOKEN_ERROR(x))
  }
}