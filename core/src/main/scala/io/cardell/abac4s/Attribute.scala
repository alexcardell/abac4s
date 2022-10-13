package io.cardell.abac4s

sealed trait Attribute {
  val key: K
  val value: V
}

object Attribute {
  type Key = String
  type Value = String

  case class Subject(key: K, value: V) extends Attribute
  case class Resource(key: K, value: V) extends Attribute
  case class Action(key: K, value: V) extends Attribute
  case class Context(key: K, value: V) extends Attribute
}
