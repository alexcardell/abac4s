package io.cardell.abac4s.syntax

import cats.data.NonEmptyChain
import cats.effect.IO
import weaver._

import io.cardell.abac4s.Denial.AttributeMissing
import io.cardell.abac4s.PolicyResult.Denied
import io.cardell.abac4s.PolicyResult.Granted
import io.cardell.abac4s._

object TestPolicies {
  def grant[A](a: A) = new Policy[IO, A] {
    def run(): IO[PolicyResult[A]] = IO.pure(Granted(a))
  }

  def deny[A](key: K) = new Policy[IO, A] {
    def run(): IO[PolicyResult[A]] = IO.pure(Denied(AttributeMissing(key)))
  }
}

object PolicySyntaxSuite extends SimpleIOSuite {
  import TestPolicies._

  val unit = ()

  test("grant and grant passes") { _ =>
    val policy = grant(unit).and(grant(unit))

    val expected = Granted((unit, unit))

    for { result <- policy.run() } yield expect(result == expected)
  }

  test("grant and denial denies") { _ =>
    val policy = grant(unit).and(deny("key"))

    val expected = Denied(AttributeMissing("key"))

    for { result <- policy.run() } yield expect(result == expected)
  }

  test("denial and grant denies") { _ =>
    val policy = deny("key").and(grant(3))

    val expected = Denied(AttributeMissing("key"))

    for { result <- policy.run() } yield expect(result == expected)
  }

  test("denial and denial denies") { _ =>
    val policy = deny("key1").and(deny("key2"))

    val expected =
      Denied(NonEmptyChain(AttributeMissing("key1"), AttributeMissing("key2")))

    for { result <- policy.run() } yield expect(result == expected)
  }
}
