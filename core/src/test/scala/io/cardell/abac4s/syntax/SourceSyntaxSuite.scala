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

    val expected = PolicyResult.Denied(Denial.AttributeKeyMissing(missingKey))

    for { result <- resourcePolicy.run() } yield expect(result == expected)
  }

  test("given the key and correct value, `resource contains` passes") {
    val resourcePolicy = attributeSource.contains(actualKey, actualValue)

    val expected = PolicyResult.Granted(exResource)

    for { result <- resourcePolicy.run() } yield expect(result == expected)
  }

  test("given no key, `resource contains` fails with AttributeKeyMissing") {
    val resourcePolicy = attributeSource.contains(actualKey, incorrectValue)

    val expected =
      PolicyResult.Denied(
        Denial.AttributeMismatch(actualKey, incorrectValue, List(actualValue))
      )

    for { result <- resourcePolicy.run() } yield expect(result == expected)
  }

  test(
    "given the key and the wrong value, `resource contains` fails with AttributeMismatch"
  ) {
    val resourcePolicy = attributeSource.contains(missingKey, actualValue)

    val expected = PolicyResult.Denied(Denial.AttributeKeyMissing(missingKey))

    for { result <- resourcePolicy.run() } yield expect(result == expected)
  }

  test(
    "given multiple instances of a key, including the expected value, `resource contains` passes"
  ) {
    val newValue = "secondValue"
    val newAttr = Resource(actualKey, newValue)

    val makeAttributes =
      (ex: ExampleResource) => ExampleResource.toAttributes(ex) ++ Set(newAttr)

    val attributeSource =
      resource[IO, ExampleResource](makeAttributes)(IO.pure(exResource))

    val resourcePolicy = attributeSource.contains(actualKey, newValue)

    val expected = PolicyResult.Granted(exResource)

    for { result <- resourcePolicy.run() } yield expect(result == expected)
  }

  test(
    "given multiple instances of a key, but not the expected value, `resource contains` passes"
  ) {
    val newValue = "secondValue"
    val newAttr = Resource(actualKey, newValue)

    val makeAttributes =
      (ex: ExampleResource) => ExampleResource.toAttributes(ex) ++ Set(newAttr)

    val attributeSource =
      resource[IO, ExampleResource](makeAttributes)(IO.pure(exResource))

    val expectedAttributeValue = "thirdValue"

    val resourcePolicy =
      attributeSource.contains(actualKey, expectedAttributeValue)

    val expected = PolicyResult.Denied(
      Denial.AttributeMismatch(
        actualKey,
        expectedAttributeValue,
        Set(actualValue, newValue).toList
      )
    )

    for { result <- resourcePolicy.run() } yield expect(result == expected)
  }
}
