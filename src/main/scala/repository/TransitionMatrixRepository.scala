package repository

import cats.effect.IO
import db.dto.{TransitionMatrix, TransitionState}
import doobie.util.transactor.Transactor
import doobie._
import doobie.implicits._
import model.{NotFoundError, TransitionMatrixCreateDTO}

class TransitionMatrixRepository(transactor: Transactor[IO]) {
  private implicit val transitionStateMeta: Meta[TransitionState] =
    Meta[String].timap(TransitionState)(_.name)

  private implicit val transitionStateSetMeta: Meta[Set[TransitionState]] =
    Meta[String]
      .timap(_.split(",").map(TransitionState).toSet)(
        _.map(_.name).mkString(",")
      )

  def getTransitionMatrixFromState(fromState: TransitionState): IO[Option[TransitionMatrix]] = {
    sql"SELECT id, from_state, possible_next_states FROM transition_matrix WHERE from_state=$fromState LIMIT 1"
      .query[TransitionMatrix]
      .option
      .transact(transactor)
  }

  def getTransitionMatrices: IO[List[TransitionMatrix]] = {
    sql"SELECT id, from_state, possible_next_states FROM transition_matrix"
      .query[TransitionMatrix]
      .stream
      .compile
      .toList
      .transact(transactor)
  }

  def getTransitionMatrixByIdUnique(id: Long): IO[TransitionMatrix] = {
    sql"SELECT id, from_state, possible_next_states FROM transition_matrix WHERE id=$id"
      .query[TransitionMatrix]
      .unique
      .transact(transactor)
  }

  def createTransitionMatrix(
      transitionMatrixCreateDTO: TransitionMatrixCreateDTO
  ): IO[TransitionMatrix] = {
    sql"INSERT INTO transition_matrix (from_state, possible_next_states) VALUES (${transitionMatrixCreateDTO.from_state}, ${transitionMatrixCreateDTO.possible_next_states})".update
      .withUniqueGeneratedKeys[Long]("id")
      .transact(transactor)
      .flatMap(getTransitionMatrixByIdUnique)
  }

  def deleteTransitionMatrix(id: Long): IO[Either[NotFoundError, Unit]] = {
    sql"DELETE FROM transition_matrix WHERE id = $id".update.run.transact(transactor).map {
      affectedRows =>
        if (affectedRows == 1) {
          Right(())
        } else {
          Left(NotFoundError())
        }
    }
  }

  def updateTransitionMatrix(
      transitionMatrix: TransitionMatrix
  ): IO[Either[NotFoundError, TransitionMatrix]] = {
    sql"UPDATE transition_matrix SET from_state=${transitionMatrix.from_state}, possible_next_states = ${transitionMatrix.possible_next_states} WHERE id = ${transitionMatrix.id}".update.run
      .transact(transactor)
      .map { affectedRows =>
        Either.cond(
          affectedRows == 1,
          transitionMatrix,
          NotFoundError()
        )
      }
  }
}
