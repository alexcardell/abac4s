package io.cardell.abac4s

import cats.data.NonEmptyChain
import cats.effect._
import cats.kernel.Eq
import io.cardell.abac4s.model._
import io.cardell.abac4s.rules._
import io.cardell.abac4s.syntax._
import weaver.{Result => WeaverResult, _}

object AttributeMustMatchRuleTest extends FunSuite {

  test("returns granted") {
    val rule    = AttributeMustMatch("subject", "alex@test.com")
    val subject = Attribute("subject", "alex@test.com")

    expect(rule(subject) == Granted)
  }

  test("returns failure") {
    val rule    = AttributeMustMatch("subject", "alex@test.com")
    val subject = Attribute("subject", "notalex@test.com")
    val expectedFailure = AttributeMustMatch
      .AttributeMustMatchFailure("subject", "alex@test.com", "notalex@test.com")
    val expected = Failure(expectedFailure)

    expect(rule(subject) == expected)
  }

  test("returns n/a when key is wrong and value would fail") {
    val rule     = AttributeMustMatch("subject", "alex@test.com")
    val subject  = Attribute("not-subject", "something else")
    val expected = NotApplicable

    expect(rule(subject) == expected)
  }

  test("returns n/a when key is wrong when value would pass") {
    val rule     = AttributeMustMatch("subject", "alex@test.com")
    val subject  = Attribute("not-subject", "alex@test.com")
    val expected = NotApplicable

    expect(rule(subject) == expected)
  }

  test("and composes with itself") {
    val lrule        = AttributeMustMatch("lattr", "lattr-ex")
    val lruleGranted = Attribute("lattr", "lattr-ex")

    val rrule        = AttributeMustMatch("rattr", "rattr-ex")
    val rruleGranted = Attribute("rattr", "rattr-ex")

    val rule = lrule.and(rrule)

    val attributeSet = Set(lruleGranted, rruleGranted)

    expect.all(
      lrule(lruleGranted) == Granted,
      lrule(rruleGranted) == NotApplicable,
      rrule(rruleGranted) == Granted,
      rrule(lruleGranted) == NotApplicable,
      rule(attributeSet) == Granted
    )
  }

  test("negation") {
    val (key, value) = ("subject", "alex@test.com")
    val rule         = AttributeMustMatch(key, value)

    val negated = rule.not

    val expected = AttributeMustNotMatch(key, value)

    expect(negated == expected)
  }
}

object AttributeMustNotMatchRuleTest extends FunSuite {

  test("returns granted") {
    val rule     = AttributeMustNotMatch("subject", "alex@test.com")
    val subject  = Attribute("subject", "something else")
    val expected = Granted

    expect(rule(subject) == Granted)
  }

  test("returns failure") {
    val rule    = AttributeMustNotMatch("subject", "alex@test.com")
    val subject = Attribute("subject", "alex@test.com")
    val expectedFailure = AttributeMustNotMatch
      .AttributeMustNotMatchFailure("subject", "alex@test.com", "alex@test.com")
    val expected = Failure(expectedFailure)

    expect(rule(subject) == expected)
  }

  test("returns n/a when key is wrong and value would fail") {
    val rule     = AttributeMustNotMatch("subject", "alex@test.com")
    val subject  = Attribute("not-subject", "alex@test.com")
    val expected = NotApplicable

    expect(rule(subject) == expected)
  }

  test("returns n/a when key is wrong and value would pass") {
    val rule     = AttributeMustNotMatch("subject", "alex@test.com")
    val subject  = Attribute("not-subject", "something else")
    val expected = NotApplicable

    expect(rule(subject) == expected)
  }

  test("and composes with itself") {
    val lrule        = AttributeMustNotMatch("lattr", "lattr-expected")
    val lruleGranted = Attribute("lattr", "not-lattr-expected")

    val rrule        = AttributeMustNotMatch("rattr", "rattr-expected")
    val rruleGranted = Attribute("rattr", "not-rattr-expected")

    val rule = lrule.and(rrule)

    val attributeSet = Set(lruleGranted, rruleGranted)

    expect.all(
      lrule(lruleGranted) == Granted,
      lrule(rruleGranted) == NotApplicable,
      rrule(rruleGranted) == Granted,
      rrule(lruleGranted) == NotApplicable,
      rule(attributeSet) == Granted
    )
  }

  test("negation") {
    val (key, value) = ("subject", "alex@test.com")
    val rule         = AttributeMustNotMatch(key, value)
    val negated      = rule.not
    val expected     = AttributeMustMatch(key, value)

    expect(negated == expected)
  }
}
