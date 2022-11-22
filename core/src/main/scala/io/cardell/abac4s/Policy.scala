package io.cardell.abac4s

import cats.Applicative
import cats.data.Validated.Invalid
import cats.data.Validated.Valid
import cats.implicits._

import io.cardell.abac4s.PolicyResult.Granted

trait Policy[F[_], A] {
  def run(): F[PolicyResult[A]]
}

object Policy {

  protected[abac4s] def apply[F[_], A](fp: F[PolicyResult[A]]): Policy[F, A] =
    new Policy[F, A] {
      def run(): F[PolicyResult[A]] = fp
    }

  def and[F[_]: Applicative, A, B](
      policy: Policy[F, A],
      other: Policy[F, B]
  ): Policy[F, (A, B)] =
    Policy(
      (policy.run(), other.run()).tupled.map {
        case (Valid(a), Valid(b))            => Granted((a, b))
        case (left @ Invalid(_), Valid(_))   => left
        case (Valid(_), right @ Invalid(_))  => right
        case (Invalid(left), Invalid(right)) => Invalid(left ++ right)
      }
    )

  def and_[F[_]: Applicative, A, B](
      policy: Policy[F, A],
      other: Policy[F, B]
  ): Policy[F, A] =
    Policy(
      and(policy, other).run().map {
        case Valid((a, _)) => Valid(a)
        case Invalid(l)    => Invalid(l)
      }
    )

  def all_[F[_]: Applicative, A](
      policies: List[Policy[F, A]]
  ): Policy[F, A] =
    Policy(
      policies.reduce(and_[F, A, A]).run()
    )

  def all_[F[_]: Applicative, A](
      policies: Policy[F, A]*
  ): Policy[F, A] =
    all_(policies.toList)

}
