package models.interop

trait CanBeHierarchic {
  val hierarchicLevel = 2
  val rootId: String
  val isRoot: Boolean
}
