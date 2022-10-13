package io.cardell.abac4s

import cats.Monad

import io.cardell.abac4s.Attribute.Action
import io.cardell.abac4s.Attribute.Context
import io.cardell.abac4s.Attribute.Resource
import io.cardell.abac4s.Attribute.Subject

package object dsl extends subject with resource with action with context

protected trait subject {
  def subject[F[_]: Monad](attrs: Set[Subject]) = AttributeSource
    .static[F, Subject](attrs)

  def subject[F[_], A](f: A => Set[Subject])(fa: F[A]) = AttributeSource
    .make[F, A, Subject](f)(fa)

  def subject[F[_], A](fa: F[A])(implicit f: A => Set[Subject]) =
    AttributeSource.make[F, A, Subject](f)(fa)
}

protected trait resource {
  def resource[F[_]: Monad](attrs: Set[Resource]) = AttributeSource
    .static[F, Resource](attrs)

  def resource[F[_], A](f: A => Set[Resource])(fa: F[A]) =
    AttributeSource.make[F, A, Resource](f)(fa)

  def resource[F[_], A](fa: F[A])(implicit f: A => Set[Resource]) =
    AttributeSource.make[F, A, Resource](f)(fa)

}

protected trait action {
  def action[F[_]: Monad](attrs: Set[Action]) = AttributeSource
    .static[F, Action](attrs)

  def action[F[_], A](f: A => Set[Action])(fa: F[A]) = AttributeSource
    .make[F, A, Action](f)(fa)

  def action[F[_], A](fa: F[A])(implicit f: A => Set[Action]) =
    AttributeSource.make[F, A, Action](f)(fa)

}

protected trait context {

  def context[F[_]: Monad](attrs: Set[Context]) = AttributeSource
    .static[F, Context](attrs)

  def context[F[_], A](f: A => Set[Context])(fa: F[A]) = AttributeSource
    .make[F, A, Context](f)(fa)

  def context[F[_], A](fa: F[A])(implicit f: A => Set[Context]) =
    AttributeSource.make[F, A, Context](f)(fa)

}
