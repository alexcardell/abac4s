package io.cardell.abac4s.syntax

import cats.implicits._
import cats.Monad
import io.cardell.abac4s.Attribute
import io.cardell.abac4s.AttributeSource
import io.cardell.abac4s.Denial
import io.cardell.abac4s.K
import io.cardell.abac4s.MiscDenial
import io.cardell.abac4s.PolicyResult
import io.cardell.abac4s.PolicyResult.Granted
import io.cardell.abac4s.PolicyResult.Denied
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

final class SourceHasKeyOps[F[_]: Monad, R, S <: Attribute](
    as: AttributeSource[F, R, S]
) {
  def hasKey(key: K): F[PolicyResult[R]] = as.source.flatMap { source =>
    val keyExists = as.attributes(source).exists(_.key == key)

    if (keyExists) Granted.as(source).pure[F]
    else MiscDenial().asInstanceOf[Denial].invalidNec.pure[F]
  }

}

final class SourceContainsOps[F[_]: Monad, R, S <: Attribute](
    as: AttributeSource[F, R, S]
) {
  def contains(key: K, value: V): F[PolicyResult[R]] = as.source
    .flatMap { source =>
      val hasMatch = as.attributes(source)
        .exists(a => a.key == key && a.value == value)

      if (hasMatch) Granted.as(source).pure[F]
      else Denied(MiscDenial()).asInstanceOf[Denial].invalidNec.pure[F]
    }

}

final class SourceMatchesOps[F[_]: Monad, R, S <: Attribute](
    left: AttributeSource[F, R, S]
) {
  def matches[R2, S2 <: Attribute](right: AttributeSource[F, R2, S2]) = new {

    def onKey(key: K): F[PolicyResult[(R, R2)]] = {
      left.source.flatMap { leftSource =>
        right.source.flatMap { rightSource =>
          val leftAttr  = left.attributes(leftSource)
          val rightAttr = right.attributes(rightSource)

          (leftAttr, rightAttr) match {
            case _ => Granted.as((leftSource, rightSource)).pure[F]
          }
        }
      }
    }
  }
}
