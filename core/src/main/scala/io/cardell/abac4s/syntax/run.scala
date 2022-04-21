package io.cardell.abac4s.syntax

import cats.syntax.applicative._
import cats.syntax.flatMap._
import cats.syntax.functor._
import cats.Monad
import io.cardell.abac4s.model._
import io.cardell.abac4s.rules._

trait run {

  trait AttributeReader[A] {
    def attributes(a: A): Set[Attribute]
  }

  implicit final class RuleFOps(self: Rule) {

    /** Secure a side effect behind this rule
      *
      * Effect will not evaluate if rule does not pass
      *
      * @tparam F
      *   effect type
      * @tparam A
      *   effect value
      * @param attributes
      *   set of attributes to apply this rule to
      * @param fa
      *   effect to run
      * @return
      *   output of effect success or failure
      */
    final def secure[F[_]: Monad, A](
        attributes: Set[Attribute]
    )(fa: F[A]): F[Either[Failure, A]] = {
      self.apply(attributes).grantedOrFail match {
        case Granted          => fa.map(Right(_))
        case failure: Failure => failure.pure[F].map(Left(_))
      }
    }

    /** Acquire a resource effectfully and only pass it along if its attributes
      * match the access policy
      *
      * Resource attributes may not be known until the resource is acquired,
      * e.g. fetching from a web server with just an identifier.
      *
      * @tparam F
      *   effect type
      * @tparam A
      *   effect value
      * @param fa
      *   effect to run
      * @param otherAttributes
      *   a set of any already acquired attributes to also include in rule
      *   application
      * @param ar
      *   AttributeReader for A
      */
    final def runF[F[_]: Monad, A, B](
        fa: F[A],
        otherAttributes: Set[Attribute] = Set.empty
    )(f: A => F[B])(implicit ar: AttributeReader[A]): F[Either[Failure, B]] = {
      fa.flatMap { a =>
        self.apply(ar.attributes(a) ++ otherAttributes).grantedOrFail match {
          case Granted          => f(a).map(Right(_))
          case failure: Failure => Monad[F].pure(failure).map(Left(_))
        }
      }
    }
  }
}
