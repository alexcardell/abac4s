package io.cardell.abac4s

import cats.syntax.applicative._
import cats.Monad

trait AttributeSource[F[_], R, S <: Attribute] {
  def source: F[R]
  def attributes(r: R): Set[S]
}

object AttributeSource {
  def apply[F[_], R, S <: Attribute](implicit
      as: AttributeSource[F, R, S]
  ): AttributeSource[F, R, S] = implicitly

  def make[F[_], R, S <: Attribute](f: R => Set[S])(fr: F[R])(implicit
      ev: S <:< Attribute
  ): AttributeSource[F, R, S] = new AttributeSource[F, R, S] {
    def source                   = fr
    def attributes(r: R): Set[S] = f(r)
  }

  def pure[F[_]: Monad, R, S <: Attribute](
      f: R => Set[S]
  )(implicit ev: S <:< Attribute): R => AttributeSource[F, R, S] = (r: R) =>
    new AttributeSource[F, R, S] {
      def source                   = r.pure[F]
      def attributes(r: R): Set[S] = f(r)
    }

  def static[F[_]: Monad, S <: Attribute](
      attr: Set[S]
  ): AttributeSource[F, Unit, S] = new AttributeSource[F, Unit, S] {
    def source                       = ().pure[F]
    def attributes(_r: Unit): Set[S] = attr
  }
}
