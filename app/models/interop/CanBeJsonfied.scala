package models.interop

import play.api.libs.json.{OWrites, Reads}

trait CanBeJsonfied[T] {
  val reads: Reads[T]
  val writes: OWrites[T]
}
