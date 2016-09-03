import biz.{AuthBiz, UserProfileBiz}
import base.{modelStatus, mongo}
import base.mongo.userFields.IdentityType
import models._
import models.interop.{HTTPResponse, HTTPResponseError, WeChatUserInfo}
import org.specs2.runner._
import org.junit.runner._
import play.api.libs.json._
import play.api.mvc.Result
import play.api.test._
import play.api.test.Helpers._

import scala.concurrent.Future

/**
  * Add your spec here.
  * You can mock out a whole application including requests, plugins etc.
  * For more information, consult the wiki.
  */
@RunWith(classOf[JUnitRunner])
class CanFakeHTTP extends CanConnectDB {
  sequential

  protected case class Uri(method: String, uri: String, auth: Boolean = true) {
    def withSimpleQuery(key: String, value: Any): Uri =
      Uri(method, uri.concat(s"?$key=$value"), auth)

    def withId(id: String, identityName: String = ":id"): Uri =
      Uri(method, uri.replace(identityName, id), auth)
  }

  /**
    * 校验ResponseResult是否符合我们的models.HTTPResponse类型
    * 并从其中Extract出我们关注的data
    */
  protected def contentValidate[T](content: Future[Result])(implicit rds: Reads[T]): T = {
    contentAsString(content) must contain("{\"ok\":")
    contentType(content) must beSome.which(_ == "application/json")
    val json = contentAsJson(content)

    (json \ "error").as[String] === ""
    (json \ "ok").as[Boolean] must beTrue
    (json \ "data").asOpt[JsValue] must beSome[JsValue]

    (json \ "status").asOpt[String] must beNone
    (json \ "message").asOpt[String] must beNone

    val response = json.validate[HTTPResponse].get
    response.ok must beTrue

    val target = response.data.validate[T]
    target must beAnInstanceOf[JsSuccess[T]]
    target.get
  }

  protected def contentError(content: Future[Result], error: HTTPResponseError) = {
    contentType(content) must beSome.which(_ == "application/json")
    val json = contentAsJson(content)

    (json \ "ok").as[Boolean] must beFalse
    (json \ "error").as[String] must not be empty
    // TODO (json \ "data").asOpt[JsValue] must beNone

    (json \ "status").asOpt[String] must beNone
    (json \ "message").asOpt[String] must beNone

    val response = json.validate[HTTPResponse].get
    response.ok must beFalse
    response.error._id must be equalTo error._id
  }

  lazy val TOKEN_QUERY_KEY = base.runtime.HTTP_AUTH2_HEADER_KEY

  protected def buildQuery(query: Map[String, Seq[String]]): String = {
    val helper = scala.collection.mutable.ListBuffer[(String, String)]()
    query.foreach{ case (queryKey, v) =>
      v.foreach { queryValue =>
        helper.append(queryKey -> queryValue)
      }
    }

    helper.map{case (k, v) => k.concat("=").concat(v)} mkString "&"
  }

  protected def withQueries[A](http: FakeRequest[A], query: Map[String, Seq[String]]): FakeRequest[A] = {
    val targetQuery = base.seqMerge[(String, Seq[String])](
      http.queryString, query, key = _._1,
      mergeItem = (x, y) => {x._1 -> (x._2 ++ y._2)})
    FakeRequest(
      method = http.method,
      uri = http.path.concat("?").concat(buildQuery(targetQuery.toMap)),
      headers = http.headers,
      body = http.body,
      remoteAddress = http.remoteAddress,
      version = http.version,
      id = http.id,
      tags = http.tags,
      secure = http.secure
    )
  }

  protected def withQuery[A](http: FakeRequest[A], query: Map[String, String]): FakeRequest[A] =
    withQueries(http, query.map{case (k, v) => k -> Seq(v)})

  protected def http(uri: Uri, payload: JsValue = JsNull, token: String = "") = uri match {
    case Uri("GET", link, true) => getAuthed(link, token)
    case Uri("GET", link, false) => get(link)
    case Uri("POST", link, true) => postAuthed(link, payload, token)
    case Uri("POST", link, false) => post(link, payload)
  }

  protected def get(uri: String) = route(FakeRequest(GET, uri)).get
  protected def getAuthed(uri: String, token: String) =
    route(FakeRequest(GET, uri).withHeaders(TOKEN_QUERY_KEY -> token)).get
  protected def post(uri: String, payload: JsValue) = {
    val response = route(FakeRequest(POST, uri)
      .withJsonBody(payload)
      .withHeaders("Content-Type" -> "application/json"))

    response.get
  }
  protected def postAuthed(uri: String, payload: JsValue, token: String) = {
    val response = route(FakeRequest(POST, uri)
      .withJsonBody(payload)
      .withHeaders(
        "Content-Type" -> "application/json",
        TOKEN_QUERY_KEY -> token
      ))

    response.get
  }

  protected object FakeUser {
    val n = 5

    val wechatOpenId1 = fakeWeChatId("kei930dke3k1ldlalei3")
    val wechatOpenId2 = fakeWeChatId("mvkd74kdklsl162ldle7")
    val wechatOpenId3 = fakeWeChatId("kekdikekl81kzzaaqkqk")
    val wechatOpenId4 = fakeWeChatId("pqlldkozkqdqzjqkkaik")
    val wechatOpenId5 = fakeWeChatId("wwkkwkkqooqokwkkekwk")


    // do not test 3rd party interop call
    private def fakeUserToken: String => String = (id: String) =>
      waitIt(
        UserProfileBiz
          .createUser(db, User.asWeChatUser(WeChatUserInfo(id, "nickname", 1, "SH")))
          .flatMap(AuthBiz.assignToken(db, _))
      )

    val token1 = fakeUserToken(wechatOpenId1)
    val token2 = fakeUserToken(wechatOpenId2)
    val token3 = fakeUserToken(wechatOpenId3)
    val token4 = fakeUserToken(wechatOpenId4)
    val token5 = fakeUserToken(wechatOpenId5)

    val tokens = Seq(token1, token2, token3, token4, token5)
  }
}
