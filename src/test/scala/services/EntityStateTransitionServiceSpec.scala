package services

import cats.effect.IO
import db.dto.{Entity, TransitionHistory, TransitionMatrix, TransitionState}
import model.{NotFoundError, TransitionHistoryCreateDTO}
import org.scalamock.scalatest.MockFactory
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import repository.{EntityRepository, TransitionHistoryRepository}

class EntityStateTransitionServiceSpec extends AnyWordSpec with MockFactory with Matchers {
  private val entityRepository            = stub[EntityRepository]
  private val transitionHistoryRepository = stub[TransitionHistoryRepository]
  private val transitionMatrixService     = stub[TransitionMatrixService]

  private val entityStateTransitionService = new EntityStateTransitionService(
    entityRepository,
    transitionHistoryRepository,
    transitionMatrixService
  )

  "EntityStateTransitionService" should {

    "transition" should {
      "should create new transition when it's allowed" in {
        val entity = Entity(
          id = 1,
          name = "entity-1"
        )

        val toState = TransitionState("to-state2")

        (entityRepository.getEntity _)
          .when(entity.id)
          .returns(IO.pure(Right(entity)))

        val oldTh = TransitionHistory(
          id = 1,
          entity_id = entity.id,
          from_state = TransitionMatrix.DEFAULT_TRANSITION_FROM_STATE,
          to_state = TransitionState("to-state1")
        )

        (transitionHistoryRepository.getLastTransition _)
          .when(entity.id)
          .returns(IO.pure(Some(oldTh)))

        val transitionHistoryCreateDTO = TransitionHistoryCreateDTO(
          entity_id = entity.id,
          from_state = oldTh.to_state,
          to_state = toState
        )

        val newTh = TransitionHistory(
          id = 2,
          entity_id = entity.id,
          from_state = TransitionMatrix.DEFAULT_TRANSITION_FROM_STATE,
          to_state = toState
        )

        (transitionHistoryRepository.createTransition _)
          .when(transitionHistoryCreateDTO)
          .returns(IO.pure(newTh))

        val tm = TransitionMatrix(
          id = 1,
          from_state = oldTh.to_state,
          possible_next_states = Set(toState)
        )

        (transitionMatrixService.getTransitionMatrixFromState _)
          .when(oldTh.to_state)
          .returns(IO.pure(Some(tm)))

        entityStateTransitionService
          .transition(entity.id, toState)
          .value
          .unsafeRunSync() shouldBe Right(newTh)
      }

      "should return Left when transition is not allowed" in {
        val entity = Entity(
          id = 1,
          name = "entity-1"
        )

        val toState = TransitionState("to-state2")

        (entityRepository.getEntity _)
          .when(entity.id)
          .returns(IO.pure(Right(entity)))

        val oldTh = TransitionHistory(
          id = 1,
          entity_id = entity.id,
          from_state = TransitionMatrix.DEFAULT_TRANSITION_FROM_STATE,
          to_state = TransitionState("to-state1")
        )

        (transitionHistoryRepository.getLastTransition _)
          .when(entity.id)
          .returns(IO.pure(Some(oldTh)))

        val transitionHistoryCreateDTO = TransitionHistoryCreateDTO(
          entity_id = entity.id,
          from_state = oldTh.to_state,
          to_state = toState
        )

        val newTh = TransitionHistory(
          id = 2,
          entity_id = entity.id,
          from_state = TransitionMatrix.DEFAULT_TRANSITION_FROM_STATE,
          to_state = toState
        )

        (transitionHistoryRepository.createTransition _)
          .when(transitionHistoryCreateDTO)
          .returns(IO.pure(newTh))

        val tm = TransitionMatrix(
          id = 1,
          from_state = oldTh.to_state,
          possible_next_states = Set(TransitionState("dummy-t"))
        )

        (transitionMatrixService.getTransitionMatrixFromState _)
          .when(oldTh.to_state)
          .returns(IO.pure(Some(tm)))

        entityStateTransitionService
          .transition(entity.id, toState)
          .value
          .unsafeRunSync() shouldBe a[Left[_, _]]
      }

      "should create new transition when no transition history found" in {
        val entity = Entity(
          id = 1,
          name = "entity-1"
        )

        val toState = TransitionState("to-state1")

        (entityRepository.getEntity _)
          .when(entity.id)
          .returns(IO.pure(Right(entity)))

        val newTh = TransitionHistory(
          id = 2,
          entity_id = entity.id,
          from_state = TransitionMatrix.DEFAULT_TRANSITION_FROM_STATE,
          to_state = toState
        )

        (transitionHistoryRepository.getLastTransition _)
          .when(entity.id)
          .returns(IO.pure(None))

        val transitionHistoryCreateDTO = TransitionHistoryCreateDTO(
          entity_id = entity.id,
          from_state = TransitionMatrix.DEFAULT_TRANSITION_FROM_STATE,
          to_state = toState
        )

        (transitionHistoryRepository.createTransition _)
          .when(transitionHistoryCreateDTO)
          .returns(IO.pure(newTh))

        entityStateTransitionService
          .transition(entity.id, toState)
          .value
          .unsafeRunSync() shouldBe Right(newTh)
      }

      "should return Not Found when Entity is missing" in {
        val entity = Entity(
          id = 1,
          name = "entity-1"
        )

        val toState = TransitionState("to-state1")

        (entityRepository.getEntity _)
          .when(entity.id)
          .returns(IO.pure(Left(NotFoundError())))

        entityStateTransitionService
          .transition(entity.id, toState)
          .value
          .unsafeRunSync() shouldBe Left(NotFoundError())
      }
    }

    "checkAndCreate" should {
      "should persist transition when it's allowed" in {
        val fromState = TransitionState("state1")
        val toState   = TransitionState("to-state1")

        val lastState = TransitionHistory(
          id = 1,
          entity_id = 1,
          from_state = toState,
          to_state = fromState
        )

        val entity = Entity(
          id = 1,
          name = "entity-1"
        )

        val tm = TransitionMatrix(
          1,
          fromState,
          Set(toState)
        )

        (transitionMatrixService.getTransitionMatrixFromState _)
          .when(fromState)
          .returns(IO.pure(Some(tm)))

        val transitionHistoryCreateDTO = TransitionHistoryCreateDTO(
          entity_id = entity.id,
          from_state = fromState,
          to_state = toState
        )

        val newTh = TransitionHistory(
          id = 2,
          entity_id = entity.id,
          from_state = fromState,
          to_state = toState
        )

        (transitionHistoryRepository.createTransition _)
          .when(transitionHistoryCreateDTO)
          .returns(IO.pure(newTh))

        entityStateTransitionService
          .checkAndCreate(lastState, entity, toState)
          .value
          .unsafeRunSync() shouldBe Right(newTh)
      }

      "should return Left when transition is not allowed" in {
        val fromState = TransitionState("state1")
        val toState   = TransitionState("to-state1")

        val lastState = TransitionHistory(
          id = 1,
          entity_id = 1,
          from_state = toState,
          to_state = fromState
        )

        val entity = Entity(
          id = 1,
          name = "entity-1"
        )

        (transitionMatrixService.getTransitionMatrixFromState _)
          .when(fromState)
          .returns(IO.pure(None))

        entityStateTransitionService.checkAndCreate(lastState, entity, toState)
      }
    }

    "checkTransitionAllowed" should {
      "should return new state if transition is allowed" in {
        val fromState = TransitionState("state1")
        val toState   = TransitionState("to-state1")

        val tm = TransitionMatrix(
          1,
          fromState,
          Set(toState)
        )

        (transitionMatrixService.getTransitionMatrixFromState _)
          .when(fromState)
          .returns(IO.pure(Some(tm)))

        entityStateTransitionService
          .checkTransitionAllowed(fromState, toState)
          .value
          .unsafeRunSync() shouldBe
          Right(toState)
      }

      "should return Left if transition is not allowed" in {
        val fromState = TransitionState("state1")
        val toState   = TransitionState("to-state1")

        val toStateFailed = TransitionState("to-state-fail")

        val tm = TransitionMatrix(
          1,
          fromState,
          Set(toState)
        )

        (transitionMatrixService.getTransitionMatrixFromState _)
          .when(fromState)
          .returns(IO.pure(Some(tm)))

        entityStateTransitionService
          .checkTransitionAllowed(fromState, toStateFailed)
          .value
          .unsafeRunSync() shouldBe a[Left[_, _]]
      }

      "should return Left if no transition matrix found" in {
        val fromState = TransitionState("state1")

        val toStateFailed = TransitionState("to-state-fail")

        (transitionMatrixService.getTransitionMatrixFromState _)
          .when(fromState)
          .returns(IO.pure(None))

        entityStateTransitionService
          .checkTransitionAllowed(fromState, toStateFailed)
          .value
          .unsafeRunSync() shouldBe a[Left[_, _]]
      }
    }
  }
}
