package controllers

import javax.inject.Inject

import biz._
import models.interop.{HTTPResponse, HTTPResponseError}
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json._
import play.api.mvc.{Action, Controller}

// Reactive Mongo imports
import play.modules.reactivemongo.{MongoController, ReactiveMongoApi, ReactiveMongoComponents}

import scala.concurrent.Future

class UserController @Inject() (val reactiveMongoApi: ReactiveMongoApi)
  extends Controller
  with MongoController with ReactiveMongoComponents
  with CanAuthenticate
  with CanResponse
  with CanCrossOrigin {

  def profile = UserAction.async { request =>
    val userId = request.userId

    UserProfileBiz.getProfile(db, userId).map {
      case Some(x) => ResponseOk(Json.toJson(x.asMasked))
      case None => ResponseError(HTTPResponseError.MONGO_NOT_FOUNT(request))
    }.map(corsGET)
  }

  def code(id: String) = Action.async {
    AuthBiz.sha1(db, id).map(ResponseOk).map(corsGET)
  }
}
