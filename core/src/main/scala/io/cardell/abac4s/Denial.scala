package io.cardell.abac4s

sealed trait Denial

object Denial {
  case class AttributeKeyMissing(key: K) extends Denial
  case class AttributeMismatch(key: K, expected: V, existingValues: List[V])
      extends Denial

  object AttributeMismatch {
    def apply(key: K, expected: V, existing: V): AttributeMismatch =
      AttributeMismatch(
        key,
        expected,
        List(existing)
      )
  }

  case class AttributeNoIntersection(key: K, left: List[V], right: List[V])
      extends Denial

  case class AttributeNotUnique(key: K, left: List[V], right: List[V])
      extends Denial
}
