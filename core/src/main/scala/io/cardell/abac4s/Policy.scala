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

  def and[F[_]: Applicative, A, B](
      policy: Policy[F, A],
      other: Policy[F, B]
  ): Policy[F, (A, B)] =
    new Policy[F, (A, B)] {

      def run(): F[PolicyResult[(A, B)]] = {
        (policy.run(), other.run()).tupled.map {
          case (Valid(a), Valid(b))            => Granted((a, b))
          case (left @ Invalid(_), Valid(_))   => left
          case (Valid(_), right @ Invalid(_))  => right
          case (Invalid(left), Invalid(right)) => Invalid(left ++ right)
        }
      }

    }

  def and_[F[_]: Applicative, A, B](
      policy: Policy[F, A],
      other: Policy[F, B]
  ): Policy[F, A] =
    new Policy[F, A] {

      def run(): F[PolicyResult[A]] = {
        and(policy, other).run().map {
          case Valid((a, _)) => Valid(a)
          case Invalid(l)    => Invalid(l)
        }
      }

    }

  def all_[F[_]: Applicative, A](
      policies: List[Policy[F, A]]
  ): Policy[F, A] =
    new Policy[F, A] {

      def run(): F[PolicyResult[A]] = {
        policies.reduce(and_[F, A, A]).run()
      }

    }

  def all_[F[_]: Applicative, A](
      policies: Policy[F, A]*
  ): Policy[F, A] =
    all_(policies.toList)

}
