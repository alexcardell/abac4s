package io.cardell.abac4s

sealed trait Denial
object Denial {
  case class AttributeMissing() extends Denial
  case class AttributeMismatch() extends Denial
}
