package models.interop

trait CanBeHierarchicInstance {
  val hierarchicLevel = 2
  val rootId: String
  def isRoot: Boolean
}

trait CanBeHierarchicObject {
  val rootFieldName = base.mongo.generalFields.hierarchicId
}