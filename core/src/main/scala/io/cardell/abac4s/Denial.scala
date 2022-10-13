package io.cardell.abac4s

sealed trait Denial
case class MiscDenial() extends Denial
