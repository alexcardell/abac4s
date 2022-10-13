package io.cardell

import cats.data.NonEmptyChain
import cats.data.Validated
import cats.data.ValidatedNec

package object abac4s {
  type K = String
  type V = String

  type PolicyResult[A] = ValidatedNec[Denial, A]
  type PolicyCheck     = PolicyResult[Unit]

  object PolicyResult {
    val Granted: PolicyCheck = Validated.validNec(())
    def Denied[A](nec: NonEmptyChain[Denial]): PolicyResult[A] = Validated
      .invalid(nec)
    def Denied[A](denial: Denial): PolicyResult[A] = Validated
      .invalidNec(denial)
  }

}
