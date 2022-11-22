package io.cardell.abac4s.syntax

import cats.Applicative

import io.cardell.abac4s.Policy

trait PolicyComposeSyntax {
  implicit def policyAndSyntax[F[_]: Applicative, A](
      policy: Policy[F, A]
  ): PolicyAndOps[F, A] = new PolicyAndOps[F, A](policy)
}

final class PolicyAndOps[F[_]: Applicative, A](policy: Policy[F, A]) {

  def and[B](other: Policy[F, B]): Policy[F, (A, B)] =
    Policy.and(policy, other)

  def and_[B](other: Policy[F, B]): Policy[F, A] =
    Policy.and_(policy, other)
}
