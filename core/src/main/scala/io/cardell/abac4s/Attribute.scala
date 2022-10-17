package io.cardell.abac4s

sealed trait Attribute {
  val key: K
  val value: V
}

object Attribute {
  type Key = String
  type Value = String

  /** Attributes about the user/service authorizing
    */
  case class Subject(key: K, value: V) extends Attribute

  /** Attributes about the resource the subject is trying to access. Likely to
    * be derived after pre-fetching the resource without exposing to the
    * subject.
    */
  case class Resource(key: K, value: V) extends Attribute

  /** Attributes about the action the subject is trying to take on the resource.
    */
  case class Action(key: K, value: V) extends Attribute

  /** Attributes covering any environment or external state of the system.
    */
  case class Context(key: K, value: V) extends Attribute
}
