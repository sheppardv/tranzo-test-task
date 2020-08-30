package repository

import cats.effect.IO
import db.dto.TransitionHistory
import doobie.implicits._
import doobie.util.transactor.Transactor
import model.TransitionHistoryCreateDTO

class TransitionHistoryRepository(transactor: Transactor[IO]) {
  def getTransitionHistories: IO[List[TransitionHistory]] = {
    sql"SELECT id, entity_id, from_state, to_state FROM transition_history"
      .query[TransitionHistory]
      .stream
      .compile
      .toList
      .transact(transactor)
  }

  def getLastTransition(entityId: Long): IO[Option[TransitionHistory]] = {
    sql"SELECT id, entity_id, from_state, to_state FROM transition_history WHERE entity_id=$entityId ORDER BY id DESC LIMIT 1"
      .query[TransitionHistory]
      .option
      .transact(transactor)
  }

  def getLastTransitionUnique(id: Long): IO[TransitionHistory] = {
    sql"SELECT id, entity_id, from_state, to_state FROM transition_history WHERE id=$id"
      .query[TransitionHistory]
      .unique
      .transact(transactor)
  }

  def createTransition(
      transitionHistoryCreateDTO: TransitionHistoryCreateDTO
  ): IO[TransitionHistory] = {
    val sql =
      sql"INSERT INTO transition_history (entity_id, from_state, to_state) VALUES (${transitionHistoryCreateDTO.entity_id}, ${transitionHistoryCreateDTO.from_state}, ${transitionHistoryCreateDTO.to_state})"

    sql.update
      .withUniqueGeneratedKeys[Long]("id")
      .transact(transactor)
      .flatMap(getLastTransitionUnique)
  }
}
