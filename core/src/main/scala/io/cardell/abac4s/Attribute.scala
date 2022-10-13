package io.cardell.abac4s

sealed trait Attribute {
  val key: K
  val value: V
}

case class Subject(key: K, value: V)  extends Attribute
case class Resource(key: K, value: V) extends Attribute
case class Action(key: K, value: V)   extends Attribute
case class Context(key: K, value: V)  extends Attribute
