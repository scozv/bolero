package controllers

import org.joda.time.{DateTime, DateTimeZone}
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json.{JsString, Json}
import play.api.mvc._
import scala.concurrent.Future

class Application extends Controller with CanCrossOrigin with CanResponse {

  def index = Action.async {
    Future.successful (Json.obj(
      "revision" -> "1443dba",
      "base" -> Json.obj(
        "mongo" -> JsString(base.runtime.conf("mongodb.uri").split("/").reverse.headOption.getOrElse(base.STRING_NIL)),
        "CORS" -> base.runtime.conf("belero.http.crossorigin")
      ),
      "release" -> new DateTime(2016, 9, 4, 17, 53, DateTimeZone.forID("Asia/Shanghai")).toString("yyyy-MM-dd HH:mm").concat(" GMT+0800")
    )).map(ResponseOk).map(corsGET)
  }
}