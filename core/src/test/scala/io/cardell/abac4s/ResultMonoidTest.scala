package io.cardell.abac4s

import _root_.io.cardell.abac4s.model._
import _root_.io.cardell.abac4s.rules._
import cats.derived._
import cats.kernel.laws.discipline.MonoidTests
import cats.kernel.laws.discipline.SemigroupTests
import cats.laws.discipline._
import cats.Eq
import cats.Monoid
import org.scalacheck.Arbitrary
import org.scalacheck.Gen
import weaver.{Result => WeaverResult, _}
import weaver.discipline._

object ResultMonoidTest extends FunSuite with Discipline {
  val genRuleFailure: Gen[RuleFailure] = for {
    key      <- Gen.alphaStr
    expected <- Gen.alphaNumStr
    received <- Gen.alphaNumStr.withFilter(_ != expected)
  } yield AttributeMustMatch.AttributeMustMatchFailure(key, expected, received)

  val genResult: Gen[Result] = Gen.oneOf(
    Gen.const(Granted),
    Gen.const(NotApplicable),
    genRuleFailure.map(Failure(_))
  )

  implicit def arbResult: Arbitrary[Result] = Arbitrary(genResult)

  implicit def eqResult: Eq[Result] = Eq.fromUniversalEquals

  checkAll("Monoid[Result]", MonoidTests[Result].monoid)
}
