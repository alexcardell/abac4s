package io.cardell.abac4s.syntax

import io.cardell.abac4s.model._
import io.cardell.abac4s.rules._

trait not {
  protected trait Invertible[R <: Rule] {
    def invert(rule: R): Rule
  }

  implicit final val attributeMustMatchInvertible =
    new Invertible[AttributeMustMatch] {
      def invert(rule: AttributeMustMatch): Rule =
        AttributeMustNotMatch(rule.key, rule.expected)
    }

  implicit final val attributeMustNotMatchInvertible =
    new Invertible[AttributeMustNotMatch] {
      def invert(rule: AttributeMustNotMatch): Rule =
        AttributeMustMatch(rule.key, rule.expected)
    }

  implicit final class RuleNotOps[R <: Rule](self: R)(implicit
      invertible: Invertible[R]
  ) {
    def not: Rule = invertible.invert(self)
  }
}
