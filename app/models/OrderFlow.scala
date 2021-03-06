package models

import models.interop.CanBeJsonfied

case class OrderFlow(status: Int, atSeconds: String)

object OrderFlow extends CanBeJsonfied[OrderFlow] {
  import play.api.libs.functional.syntax._
  import play.api.libs.json._

  implicit val writes = new OWrites[OrderFlow] {
    def writes(x: OrderFlow) = Json.obj (
      "status" -> x.status,
      "atSeconds" -> x.atSeconds
    )
  }

  implicit val reads: Reads[OrderFlow] = (
    (__ \ "status").read[Int] and
      (__ \ "atSeconds").read[String]
    )(OrderFlow.apply _)
}