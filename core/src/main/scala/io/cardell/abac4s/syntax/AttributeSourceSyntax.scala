package io.cardell.abac4s.syntax

import cats.Monad
import cats.implicits._

import io.cardell.abac4s.Attribute
import io.cardell.abac4s.AttributeSource
import io.cardell.abac4s.Denial.AttributeKeyMissing
import io.cardell.abac4s.Denial.AttributeMismatch
import io.cardell.abac4s.Denial.AttributeNoIntersection
import io.cardell.abac4s.Denial.AttributeNotUnique
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

trait Matcher[F[_], A] {
  def onKey(key: K): Policy[F, A]
}

final class SourceMatchesOps[F[_]: Monad, R, S <: Attribute](
    first: AttributeSource[F, R, S]
) {

  def matches[R2, S2 <: Attribute](second: AttributeSource[F, R2, S2]) = new {

    def intersect = new Matcher[F, (R, R2)] {

      def onKey(key: K): Policy[F, (R, R2)] = Policy {

        for {
          source1 <- first.source
          source2 <- second.source

          attr1 = first.attributes(source1).filter(_.key == key).map(_.value)
          attr2 = second.attributes(source2).filter(_.key == key).map(_.value)

          intersection = attr1.intersect(attr2)

          res = (attr1.toList, attr2.toList, intersection.toList) match {
            // TODO tag which side is missing
            case (Nil, Nil, _)    => Denied(AttributeKeyMissing(key))
            case (_ :: _, Nil, _) => Denied(AttributeKeyMissing(key))
            case (Nil, _ :: _, _) => Denied(AttributeKeyMissing(key))
            case (l1 @ _ :: _, l2 @ _ :: _, Nil) =>
              Denied(AttributeNoIntersection(key, l1, l2))
            case (_ :: _, _ :: _, _ :: _) =>
              Granted((source1, source2))
          }
        } yield res
      }
    }

    def unique = new Matcher[F, (R, R2)] {

      def onKey(key: K): Policy[F, (R, R2)] = Policy {

        for {
          source1 <- first.source
          source2 <- second.source

          attr1 = first.attributes(source1).filter(_.key == key).map(_.value)
          attr2 = second.attributes(source2).filter(_.key == key).map(_.value)
          intersection = attr1.intersect(attr2)

          res = (attr1.toList, attr2.toList, intersection.toList) match {
            case (Nil, Nil, _)    => Denied(AttributeKeyMissing(key))
            case (_ :: _, Nil, _) => Denied(AttributeKeyMissing(key))
            case (Nil, _ :: _, _) => Denied(AttributeKeyMissing(key))
            case (l1 @ _ :: _, l2 @ _ :: _, Nil) =>
              Denied(AttributeNoIntersection(key, l1, l2))
            case (_ :: Nil, _ :: Nil, _ :: Nil) =>
              Granted((source1, source2))
            case (l1 @ _ :: _, l2 @ _ :: _, _ :: _) =>
              Denied(AttributeNotUnique(key, l1, l2))
          }
        } yield res
      }
    }

  }

}
