package controllers

import models.interop.HTTPResponse
import org.joda.time.DateTime
import play.api.libs.json.{JsString, Json}
import play.api.mvc._
import scala.concurrent.Future
import play.api.libs.concurrent.Execution.Implicits.defaultContext

class Application extends Controller with CanCrossOrigin with CanResponse {

  def index = Action.async {
    Future.successful (Json.obj(
      "revision" -> "8134a3a",
      "base" -> Json.obj(
        "mongo" -> JsString(base.runtime.conf("mongodb.uri").split("/").reverse.headOption.getOrElse(base.STRING_NIL)),
        "CORS" -> base.runtime.conf("belero.http.crossorigin")
      ),
      "release" -> new DateTime(2016, 7, 27, 7, 48).toString("yyyy-MM-dd HH:mm")
    )).map(ResponseOk).map(corsGET)
  }

}