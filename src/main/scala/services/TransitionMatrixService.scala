package services

import cats.data.EitherT
import cats.effect.IO
import db.dto.{TransitionMatrix, TransitionState}
import model.{AppError, AppResp, NotFoundError, TransitionMatrixCreateDTO}
import repository.TransitionMatrixRepository

class TransitionMatrixService(
    transitionMatrixRepository: TransitionMatrixRepository
) {

  def createTransitionMatrix(
      transitionMatrixCreateDTO: TransitionMatrixCreateDTO
  ): AppResp[TransitionMatrix] = {
    EitherT
      .liftF(
        transitionMatrixRepository.getTransitionMatrixFromState(
          transitionMatrixCreateDTO.from_state
        )
      )
      .flatMap {
        case Some(matrix) =>
          val matrixToUpdate = matrix.copy(
            possible_next_states =
              matrix.possible_next_states ++ transitionMatrixCreateDTO.possible_next_states
          )

          EitherT[IO, AppError, TransitionMatrix](
            transitionMatrixRepository.updateTransitionMatrix(matrixToUpdate)
          ).flatMap { matrix =>
            EitherT.liftF(TransitionMatrixCache.insert(matrix.from_state, matrix))
              .map(_ => matrix)
          }
        case None =>
          EitherT.liftF(
            transitionMatrixRepository.createTransitionMatrix(transitionMatrixCreateDTO)
          )
      }
  }

  def getTransitionMatrixList: IO[List[TransitionMatrix]] =
    transitionMatrixRepository.getTransitionMatrices

  def deleteTransitionMatrix(id: Long): AppResp[Unit] =
    for {
      matrix <- EitherT.fromOptionF(
        transitionMatrixRepository.getTransitionMatrix(id),
        NotFoundError()
      )
      _ <- EitherT.liftF(TransitionMatrixCache.delete(matrix.from_state))
      _ <- EitherT[IO, AppError, Unit](transitionMatrixRepository.deleteTransitionMatrix(id))
    } yield ()

  def getTransitionMatrixFromState(fromState: TransitionState): IO[Option[TransitionMatrix]] = {
    TransitionMatrixCache.retrieve(fromState).flatMap {
      case Some(matrix) =>
        IO.pure(Some(matrix))

      case None =>
        transitionMatrixRepository.getTransitionMatrixFromState(fromState).flatMap {
          case Some(matrix) =>
            TransitionMatrixCache.insert(matrix.from_state, matrix).map(_ => Some(matrix))
          case None =>
            IO.pure(None)
        }
    }
  }
}
