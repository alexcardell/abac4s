package io.cardell.abac4s

import cats.effect.IO
import cats.implicits._
import io.cardell.abac4s._
import io.cardell.abac4s.dsl._
import io.cardell.abac4s.implicits._
import weaver.SimpleIOSuite

object MainSuite extends SimpleIOSuite {

  case class ExampleResource(owner: String, data: Int)

  val ownerKey = "owner"

  object ExampleResource {
    val toAttributes =
      (er: ExampleResource) => Set(Resource(ownerKey, er.owner))
  }

  test(s"given key, `resource hasKey` passes") {
    val exResourceData = 33
    val exResource     = ExampleResource("mr. anderson", 33)

    val getResource = IO.pure(exResource)

    val resourcePolicy = {
      resource[IO, ExampleResource](ExampleResource.toAttributes)
        .andThen(_.hasKey(ownerKey))
    }

    val expected = PolicyResult.Granted.as(exResource)

    for { result <- resourcePolicy(getResource) } yield expect(result == expected)
  }

  test(s"given no key, `resource hasKey` passes") {
    val missingKey = "missingKey"

    val exResourceData = 33
    val exResource     = ExampleResource("mr. anderson", 33)

    val getResource = IO.pure(exResource)

    val resourcePolicy = {
      resource[IO, ExampleResource](ExampleResource.toAttributes)
        .andThen(_.hasKey(missingKey))
    }

    val expected = PolicyResult.Denied(MiscDenial())

    for { result <- resourcePolicy(getResource) } yield expect(result == expected)
  }
}
