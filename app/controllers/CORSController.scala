package controllers

import biz._
import base.mongo
import models._
import play.api.libs.json.{JsValue, JsError, Json}
import play.api.mvc.{Action, Controller}
import play.modules.reactivemongo.MongoController
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import mongo.userFields.IdentityType

import scala.concurrent.Future
import scala.util.Try

import javax.inject.Inject
import play.api.libs.ws._

import scala.concurrent.Future

import play.api.Logger
import play.api.mvc.{ Action, Controller }
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json._

// Reactive Mongo imports
import reactivemongo.api.Cursor

import play.modules.reactivemongo.{ // ReactiveMongo Play2 plugin
MongoController,
ReactiveMongoApi,
ReactiveMongoComponents
}

// BSON-JSON conversions/collection
import play.modules.reactivemongo.json._
import play.modules.reactivemongo.json.collection._

class CORSController
  extends Controller
  with CanCrossOrigin {

  def preFlight(path: String) = Action { request =>
    corsOPTION(path)
  }

}
