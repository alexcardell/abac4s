package io.cardell.abac4s.syntax

import cats.Monad
import cats.implicits._

import io.cardell.abac4s.Attribute
import io.cardell.abac4s.AttributeSource
import io.cardell.abac4s.Denial.AttributeKeyMissing
import io.cardell.abac4s.Denial.AttributeMismatch
import io.cardell.abac4s.K
import io.cardell.abac4s.Policy
import io.cardell.abac4s.PolicyResult.Denied
import io.cardell.abac4s.PolicyResult.Granted
import io.cardell.abac4s.V

trait AttributeSourceSyntax {
  implicit final def sourceHasKeySyntax[F[_]: Monad, R, S <: Attribute](
      as: AttributeSource[F, R, S]
  ): SourceHasKeyOps[F, R, S] = new SourceHasKeyOps(as)

  implicit final def sourceContainsSyntax[F[_]: Monad, R, S <: Attribute](
      as: AttributeSource[F, R, S]
  ): SourceContainsOps[F, R, S] = new SourceContainsOps(as)

  implicit final def sourceMatchesSyntax[F[_]: Monad, R, S <: Attribute](
      as: AttributeSource[F, R, S]
  ): SourceMatchesOps[F, R, S] = new SourceMatchesOps(as)
}

final class SourceHasKeyOps[F[_]: Monad, A, S <: Attribute](
    as: AttributeSource[F, A, S]
) {

  def hasKey(key: K): Policy[F, A] = Policy {

    as.source.flatMap { source =>
      val keyExists = as.attributes(source).exists(_.key == key)

      if (keyExists) Granted().as(source).pure[F]
      else Denied(AttributeKeyMissing(key)).pure[F]
    }

  }

}

final class SourceContainsOps[F[_]: Monad, A, S <: Attribute](
    as: AttributeSource[F, A, S]
) {

  def contains(key: K, value: V): Policy[F, A] = Policy {

    as.source.map { source =>
      val attrs = as
        .attributes(source)
        .filter(_.key == key)
        .map(_.value)
        .toList

      attrs match {
        case ls if ls.exists(_ == value) => Granted(source)
        case ls @ _ :: _ => Denied(AttributeMismatch(key, value, ls))
        case Nil         => Denied(AttributeKeyMissing(key))
      }

    }
  }

}

final class SourceMatchesOps[F[_]: Monad, R, S <: Attribute](
    first: AttributeSource[F, R, S]
) {

  def matches[R2, S2 <: Attribute](second: AttributeSource[F, R2, S2]) = new {

    def onKey(key: K): Policy[F, (R, R2)] = Policy {

      for {
        source1 <- first.source
        source2 <- second.source
        attr1 = first.attributes(source1).find(_.key == key)
        attr2 = second.attributes(source2).find(_.key == key)
        res = (attr1, attr2) match {
          case (None, _) => Denied(AttributeKeyMissing(key))
          case (_, None) => Denied(AttributeKeyMissing(key))
          case (Some(l), Some(r)) if (l.value == r.value) =>
            Granted((source1, source2))
          case (Some(l), Some(r)) =>
            Denied(AttributeMismatch(key, l.value, r.value))
        }
      } yield res
    }

  }

}
