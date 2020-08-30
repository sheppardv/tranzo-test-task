package services

import cats.data.EitherT
import cats.effect.IO
import db.dto.{Entity, TransitionHistory, TransitionMatrix, TransitionState}
import model.{AppError, InvalidTransitionError, TransitionHistoryCreateDTO}
import repository.{EntityRepository, TransitionHistoryRepository, TransitionMatrixRepository}

class EntityStateTransitionService(
    entityRepository: EntityRepository,
    transitionHistoryRepository: TransitionHistoryRepository,
    transitionMatrixRepository: TransitionMatrixRepository
) {
  type AppResp[A] = EitherT[IO, AppError, A]

  def transition(
      entityId: Long,
      toState: TransitionState
  ): AppResp[TransitionHistory] = {
    for {
      entity      <- EitherT(entityRepository.getEntity(entityId))
      mbLastState <- EitherT.liftF(transitionHistoryRepository.getLastTransition(entity.id))
      transitionHistory <- mbLastState match {
        case Some(lastSate) =>
          checkAndCreate(lastSate, entity, toState)

        case None =>
          val transitionDTO = TransitionHistoryCreateDTO(
            entity_id = entity.id,
            from_state = TransitionMatrix.DEFAULT_TRANSITION_FROM_STATE,
            to_state = toState
          )

          EitherT.liftF[IO, AppError, TransitionHistory](
            transitionHistoryRepository.createTransition(transitionDTO)
          )
      }
    } yield transitionHistory
  }

  private def checkAndCreate(
      lastSate: TransitionHistory,
      entity: Entity,
      toState: TransitionState
  ): EitherT[IO, AppError, TransitionHistory] = {
    val transitionDTO = TransitionHistoryCreateDTO(
      entity_id = entity.id,
      from_state = lastSate.to_state,
      to_state = toState
    )

    for {
      _ <- checkTransitionAllowed(lastSate.to_state, toState)
      newTransition <- EitherT.liftF(
        transitionHistoryRepository.createTransition(transitionDTO)
      ): AppResp[TransitionHistory]
    } yield newTransition
  }

  def checkTransitionAllowed(
      fromState: TransitionState,
      toState: TransitionState
  ): EitherT[IO, InvalidTransitionError, TransitionState] = {
    for {
      transitionMatrix <- EitherT.fromOptionF(
        transitionMatrixRepository.getTransitionMatricesFromState(fromState),
        InvalidTransitionError(s"Transition from ${fromState.name} is not configured.")
      )
      newState <- EitherT.cond[IO](
        transitionMatrix.possible_next_states.contains(toState),
        toState,
        InvalidTransitionError(
          s"Transition from ${fromState.name} to ${toState.name} is not allowed. Possible new states: ${transitionMatrix.possible_next_states
            .map(_.name)}"
        )
      )
    } yield newState
  }
}
