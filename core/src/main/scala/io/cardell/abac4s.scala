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
    def Granted[A](a: A): PolicyResult[A] = Validated.validNec(a)
    val Granted: PolicyCheck = Granted(())
    def Denied[A](nec: NonEmptyChain[Denial]): PolicyResult[A] = Validated
      .invalid(nec)
    def Denied[A](one: Denial, tail: Denial*): PolicyResult[A] = Validated
      .invalid(NonEmptyChain.of(one, tail: _*))
    def Denied[A](denial: Denial): PolicyResult[A] = Validated
      .invalidNec(denial)
  }

}
