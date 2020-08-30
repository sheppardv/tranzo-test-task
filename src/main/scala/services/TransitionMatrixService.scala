package services

import cats.data.EitherT
import cats.effect.IO
import db.dto.TransitionMatrix
import model.{AppError, AppResp, TransitionMatrixCreateDTO}
import repository.TransitionMatrixRepository

class TransitionMatrixService(
    transitionMatrixRepository: TransitionMatrixRepository
) {

  def createTransitionMatrix(transitionMatrixCreateDTO: TransitionMatrixCreateDTO): AppResp[TransitionMatrix] = {
    EitherT
      .liftF(
        transitionMatrixRepository.getTransitionMatricesFromState(
          transitionMatrixCreateDTO.from_state
        )
      )
      .flatMap {
        case Some(matrix) =>
          val matrixToUpdate = matrix.copy(
            possible_next_states =
              matrix.possible_next_states ++ transitionMatrixCreateDTO.possible_next_states
          )

          EitherT[IO, AppError, TransitionMatrix](transitionMatrixRepository.updateTransitionMatrix(matrixToUpdate))
        case None =>
          EitherT.liftF(
            transitionMatrixRepository.createTransitionMatrix(transitionMatrixCreateDTO)
          )
      }
  }

  def getTransitionMatrixList: IO[List[TransitionMatrix]] =
    transitionMatrixRepository.getTransitionMatrices

  def deleteTransitionMatrix(id: Long): AppResp[Unit] =
    EitherT(transitionMatrixRepository.deleteTransitionMatrix(id))
}
