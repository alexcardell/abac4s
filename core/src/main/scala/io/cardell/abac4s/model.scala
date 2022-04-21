package io.cardell.abac4s

import cats.data.NonEmptyChain
import cats.data.ValidatedNec
import cats.implicits._
import cats.kernel.Group
import cats.kernel.Monoid
import cats.kernel.Semigroup
import cats.Monad

package object model {
  case class Attribute(key: String, value: String)

  sealed trait RuleFailure
  case object NotApplicableFailure extends RuleFailure

  sealed trait Result {
    self =>

    def grantedOrFail: ConcreteResult = self match {
      case NotApplicable         => Failure(NonEmptyChain(NotApplicableFailure))
      case other: ConcreteResult => other
    }
  }

  sealed trait ConcreteResult extends Result
  case object NotApplicable   extends Result
  case object Granted         extends ConcreteResult
  case class Failure(failures: NonEmptyChain[RuleFailure])
      extends ConcreteResult

  object Failure {
    def apply(failure: RuleFailure): Result = Failure(NonEmptyChain(failure))
  }

  object Result {
    implicit def ResultMonoid: Monoid[Result] = new Monoid[Result] {
      def empty: Result = NotApplicable

      def combine(x: Result, y: Result): Result = (x, y) match {
        case (Granted, Granted)             => Granted
        case (NotApplicable, Granted)       => Granted
        case (Granted, NotApplicable)       => Granted
        case (NotApplicable, NotApplicable) => NotApplicable
        case (Failure(le), Failure(re))     => Failure(le ++ re)
        case (l @ Failure(_), _)            => l
        case (_, r @ Failure(_))            => r
      }
    }
  }

  trait Rule {
    self =>

    protected[abac4s] def apply(attribute: Attribute): Result

    protected[abac4s] def apply(key: String, value: String): Result = self
      .apply(Attribute(key, value))

    protected[abac4s] def apply(attributeSet: Set[Attribute]): Result =
      Monoid[Result].combineAll(attributeSet.map(self.apply))

    def apply(attributes: Map[String, String]): Result = {
      val attributeSet = attributes.toSet
        .map((s: (String, String)) => Attribute(s._1, s._2))

      self.apply(attributeSet)
    }
  }
}

package object rules {
  import model._

  case class AttributeMustMatch(key: String, expected: String) extends Rule {
    def apply(attribute: Attribute): Result = {
      (attribute.key, attribute.value) match {
        case (k, _) if k != key                                => NotApplicable
        case (k, received) if k == key && expected == received => Granted
        case (k, received) => Failure(
            AttributeMustMatch
              .AttributeMustMatchFailure(key, expected, received)
          )
      }
    }
  }

  object AttributeMustMatch {
    case class AttributeMustMatchFailure(
        key: String,
        expected: String,
        received: String
    ) extends RuleFailure
  }

  case class AttributeMustNotMatch(key: String, expected: String) extends Rule {
    def apply(attribute: Attribute): Result = {
      (attribute.key, attribute.value) match {
        case (k, _) if k != key                                => NotApplicable
        case (k, received) if k == key && expected != received => Granted
        case (k, received) => Failure(
            AttributeMustNotMatch
              .AttributeMustNotMatchFailure(key, expected, received)
          )
      }
    }
  }

  object AttributeMustNotMatch {
    case class AttributeMustNotMatchFailure(
        key: String,
        expected: String,
        received: String
    ) extends RuleFailure
  }
}
