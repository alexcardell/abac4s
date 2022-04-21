package io.cardell.abac4s.syntax

import cats.syntax.monoid._
import io.cardell.abac4s.model._
import io.cardell.abac4s.rules._

trait and {
  implicit final class RuleAndOps(self: Rule) {

    final def and(rhs: Rule): Rule = new Rule {

      def apply(attribute: Attribute): Result = {
        val left  = self.apply(attribute)
        val right = rhs.apply(attribute)

        left.combine(right)
      }
    }
  }
}
