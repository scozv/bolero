package models.interop

import play.api.http.Writeable

trait CanBeBytefied[T] /*exten implicit writeable: Writeable[C] */ {
  val byteFormats: Writeable[T]
}
