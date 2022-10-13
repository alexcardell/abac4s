package io.cardell.abac4s

import cats.effect.IO
import weaver._

import io.cardell.abac4s.Attribute.Resource
import io.cardell.abac4s._
import io.cardell.abac4s.dsl._
import io.cardell.abac4s.syntax._

object SourceSyntaxSuite extends SimpleIOSuite {

  val actualKey = "owner"
  val missingKey = "missing key"
  val actualValue = "mr. anderson"
  val incorrectValue = "agent smith"

  case class ExampleResource(owner: String, data: Int)

  object ExampleResource {
    val toAttributes =
      (er: ExampleResource) => Set(Resource(actualKey, er.owner))
  }

  val exResource = ExampleResource(owner = actualValue, data = 33)

  val attributeSource = resource[IO, ExampleResource](
    ExampleResource.toAttributes
  )(IO.pure(exResource))

  test("given a key, `resource hasKey` passes") {
    val resourcePolicy = attributeSource.hasKey(actualKey)
    val expected = PolicyResult.Granted(exResource)

    for { result <- resourcePolicy.run() } yield expect(result == expected)
  }

  test("given no key, `resource hasKey` fails") {
    val resourcePolicy = attributeSource.hasKey(missingKey)

    val expected = PolicyResult.Denied(Denial.AttributeMissing())

    for { result <- resourcePolicy.run() } yield expect(result == expected)
  }

  test("given the key and correct value, `resource contains` passes") {
    val resourcePolicy = attributeSource.contains(actualKey, actualValue)

    val expected = PolicyResult.Granted(exResource)

    for { result <- resourcePolicy.run() } yield expect(result == expected)
  }

  test("given no key, `resource contains` fails with AttributeMissing") {
    val resourcePolicy = attributeSource.contains(actualKey, incorrectValue)

    val expected = PolicyResult.Denied(Denial.AttributeMismatch())

    for { result <- resourcePolicy.run() } yield expect(result == expected)
  }

  test(
    "given the key and the wrong value, `resource contains` fails with AttributeMismatch"
  ) {
    val resourcePolicy = attributeSource.contains(missingKey, actualValue)

    val expected = PolicyResult.Denied(Denial.AttributeMissing())

    for { result <- resourcePolicy.run() } yield expect(result == expected)
  }
}
