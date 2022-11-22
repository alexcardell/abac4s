# abac4s

[![release](https://github.com/alexcardell/abac4s/actions/workflows/ci.yaml/badge.svg)](https://github.com/alexcardell/abac4s/actions/workflows/ci.yaml)
[![version](https://img.shields.io/maven-central/v/io.cardell/abac4s_2.13)](https://search.maven.org/artifact/io.cardell/abac4s_2.13)

Access Based Access Control for Scala.

Provides composable attribute rules to build policies,
and utilities for securing effectful operations behind 
policies.

Very WIP.

## Installing 

```
libraryDependencies += "io.cardell" %% "abac4s-core" % "@VERSION@"
```

## Usage

```scala mdoc
import cats.effect.IO

import io.cardell.abac4s.Attribute
import io.cardell.abac4s.PolicyResult
import io.cardell.abac4s.dsl._
import io.cardell.abac4s.syntax._

type Token = String
// example function extracting attributes
val tokenAttrs: Token => Set[Attribute.Subject] = _ => Set.empty
val token: IO[Token] = IO.pure("token")

val subjectSource = subject[IO, Token](tokenAttrs)(token)
val subjectPolicy = subjectSource.hasKey("key")
val subjectPolicyResult: IO[PolicyResult[Token]] = subjectPolicy.run()

// composing
case class MyResource()
val fetch: IO[MyResource] = IO.pure(MyResource())
val resourceAttrs: MyResource => Set[Attribute.Resource] = _ => Set.empty
val resourceSource = resource[IO, MyResource](resourceAttrs)(fetch)

val resourcePolicy = resourceSource.contains("key", "value")

val resourcePolicyResult: IO[PolicyResult[MyResource]] = resourcePolicy.run()

val combinedResult: IO[PolicyResult[(Token, MyResource)]] = 
  (subjectPolicy and resourcePolicy).run() 

// matching
// checks the two attribute sources share the same value for a key
val matchPolicy = (subjectSource matches resourceSource).onKey("key")

val matchPolicyResult: IO[PolicyResult[(Token, MyResource)]] = matchPolicy.run()
```

## License

This software is licensed under the MIT license. See [LICENSE](./LICENSE)

## Developing

To set up development dependencies use Nix >2.4
with flakes enabled, and use the `nix develop` command.
