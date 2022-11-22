package io.cardell.abac4s.syntax

import cats.Applicative
import cats.MonadThrow
import cats.data.Validated
import cats.implicits._

import io.cardell.abac4s.Denial
import io.cardell.abac4s.PolicyResult

trait PolicyResultFSyntax {
  implicit def policyResultFSyntax[F[_]: MonadThrow, A](
      fr: F[PolicyResult[A]]
  ): PolicyResultFOps[F, A] = new PolicyResultFOps[F, A](fr)
}

final class PolicyResultFOps[F[_]: MonadThrow, A](
    fr: F[PolicyResult[A]]
) {

  def raiseDenial(f: List[Denial] => Throwable): F[A] =
    fr.flatMap {
      case Validated.Valid(a) => Applicative[F].pure(a)
      case Validated.Invalid(denials) =>
        MonadThrow[F].raiseError(f(denials.toList))
    }

}
