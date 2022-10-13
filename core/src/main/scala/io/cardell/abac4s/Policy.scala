package io.cardell.abac4s

trait Policy[F[_], A] {
  def run(): F[PolicyResult[A]]
}
