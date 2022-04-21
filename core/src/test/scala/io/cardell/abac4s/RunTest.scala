package io.cardell.abac4s

import cats.effect.IO
import cats.implicits._
import io.cardell.abac4s.model._
import io.cardell.abac4s.rules._
import io.cardell.abac4s.syntax._
import weaver.{Result => WeaverResult, _}

object RunTest extends SimpleIOSuite {
  val (k, v)    = ("test-attr", "expected-value")
  val rule      = AttributeMustMatch(k, v)
  val operation = IO(42)

  test("secure with passing rule runs an operation") {
    val input    = Set(Attribute(k, v))
    val expected = Right(42)

    rule.secure(input)(operation).map(output => expect(output == expected))
  }

  test("secure with passing rule runs an operation") {
    val mismatchedValue = "not-expected-value"
    val input           = Set(Attribute(k, mismatchedValue))
    val expected = Left(Failure(
      AttributeMustMatch.AttributeMustMatchFailure(k, v, mismatchedValue)
    ))

    rule.secure(input)(operation).map(output => expect(output == expected))
  }
}
