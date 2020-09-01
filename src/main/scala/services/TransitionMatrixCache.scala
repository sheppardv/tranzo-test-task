package services

import scalacache.Mode
import scalacache._
import scalacache.caffeine._
import cats.effect.IO
import db.dto.{TransitionMatrix, TransitionState}


object TransitionMatrixCache {
  implicit val mode: Mode[IO] = scalacache.CatsEffect.modes.async
  implicit val transitionMatrixCache: Cache[TransitionMatrix] = CaffeineCache[TransitionMatrix]

  def insert(fromState: TransitionState, matrix: TransitionMatrix): IO[Any] =
    put(fromState.name)(matrix)

  def retrieve(fromState: TransitionState): IO[Option[TransitionMatrix]] =
    get(fromState.name)

  def delete(fromState: TransitionState): IO[Any] =
    remove(fromState.name)
}
