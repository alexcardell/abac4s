package io.cardell.abac4s

import cats.Monad

package object dsl {
  def subject[F[_]: Monad](attrs: Set[Subject]) = AttributeSource
    .static[F, Subject](attrs)

  def resource[F[_]: Monad, R](
      f: R => Set[Resource]
  ): (F[R]) => AttributeSource[F, R, Resource] =
    AttributeSource.make[F, R, Resource](f)(_)
}
