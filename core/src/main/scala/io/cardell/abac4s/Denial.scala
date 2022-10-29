package io.cardell.abac4s

sealed trait Denial

object Denial {
  case class AttributeMissing(key: K) extends Denial
  case class AttributeMismatch(key: K, left: V, right: V) extends Denial
}
