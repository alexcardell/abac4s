package io.cardell.abac4s.syntax

import cats.Monad
import cats.data.Validated.Invalid
import cats.data.Validated.Valid
import cats.implicits._

import io.cardell.abac4s.Policy
import io.cardell.abac4s.PolicyResult
import io.cardell.abac4s.PolicyResult.Granted

trait PolicyComposeSyntax {
  implicit def policyAndSyntax[F[_]: Monad, A](
      policy: Policy[F, A]
  ): PolicyAndOps[F, A] = new PolicyAndOps[F, A](policy)
}

final class PolicyAndOps[F[_]: Monad, A](policy: Policy[F, A]) {

  def and[B](other: Policy[F, B]): Policy[F, (A, B)] = new Policy[F, (A, B)] {

    def run(): F[PolicyResult[(A, B)]] = {
      (policy.run(), other.run()).tupled.map {
        case (Valid(a), Valid(b))            => Granted((a, b))
        case (left @ Invalid(_), Valid(_))   => left
        case (Valid(_), right @ Invalid(_))  => right
        case (Invalid(left), Invalid(right)) => Invalid(left ++ right)
      }
    }

  }
}
