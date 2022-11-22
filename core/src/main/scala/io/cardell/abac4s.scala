package io.cardell

import cats.data.NonEmptyChain
import cats.data.Validated
import cats.data.ValidatedNec

package object abac4s {
  type K = Attribute.Key
  type V = Attribute.Value

  type PolicyResult[A] = ValidatedNec[Denial, A]
  type PolicyCheck = PolicyResult[Unit]

  object PolicyResult {

    object Granted {

      def apply[A](a: A): PolicyResult[A] = Validated.validNec(a)

      def apply(): PolicyCheck = apply[Unit](())

      def unapply[A](pc: PolicyResult[A]): Option[A] =
        pc match {
          case Validated.Valid(a) => Some(a)
          case _                  => None
        }
    }

    object Denied {
      def apply[A](nec: NonEmptyChain[Denial]): PolicyResult[A] = Validated
        .invalid(nec)

      def apply[A](one: Denial, tail: Denial*): PolicyResult[A] = Validated
        .invalid(NonEmptyChain.of(one, tail: _*))

      def apply[A](denial: Denial): PolicyResult[A] = Validated
        .invalidNec(denial)

      def unapply[A](pc: PolicyResult[A]): Option[NonEmptyChain[Denial]] =
        pc match {
          case Validated.Valid(_)     => None
          case Validated.Invalid(nec) => Some(nec)
        }

    }
  }

}
