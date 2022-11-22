package io.cardell.abac4s.syntax

import cats.Functor
import cats.implicits._

import io.cardell.abac4s.Policy

trait PolicySyntax {

  implicit def policySyntax[F[_]: Functor, A](
      policy: Policy[F, A]
  ): PolicyOps[F, A] = new PolicyOps[F, A](policy)

}

final class PolicyOps[F[_]: Functor, A](policy: Policy[F, A]) {

  def void: Policy[F, Unit] =
    new Policy[F, Unit] {
      def run() = policy.run().map(_.void)
    }
}
