# abac4s

Access Based Access Control for Scala.

Provides composable attribute rules to build policies,
and utilities for securing effectful operations behind 
policies.

Very WIP.

## Installing 

:construction: Deploying to Sonatype soon 

## Usage

```scala
import io.cardell.abac4s.Attribute.Subject
import io.cardell.abac4s.Attribute.Resource
import io.cardell.abac4s.dsl._
import io.cardell.abac4s.syntax._

type Token = ???
val tokenAttrs: Token => Set[Subject]
val token: IO[Token] = ???

val subjectSource = subject[IO, Token](tokenAttrs)(token)
val subjectPolicy = subjectSource.hasKey("key")
val subjectPolicyResult: IO[PolicyResult[Token]] = subjectPolicy.run()

// composing
case class MyResource()
val fetch: IO[Resource] = ???
val resourceAttrs: MyResource => Set[Resource]
val resourceSource = resource[IO, MyResource](resourceAttrs)(fetch)

val resourcePolicy = resourceSource.contains("key", "value")

val combinedResult = (subjectPolicy and resourcePolicy).run() 

// matching
// checks the two attribute sources share the same value for a key
(subjectSource matches resourceSource).onKey("key")
```

## License

This software is licensed under the MIT license. See [LICENSE](./LICENSE)

## Developing

To set up development dependencies use Nix >2.4
with flakes enabled, and use the `nix develop` command.
