package controllers

import cats.effect.IO
import io.circe.syntax._
import model.EntityUpdateStateDTO
import org.http4s.HttpRoutes
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl
import services.EntityStateTransitionService

class EntityTransitionController(entityStateTransitionService: EntityStateTransitionService)
    extends Http4sDsl[IO] {

  val routes: HttpRoutes[IO] = HttpRoutes.of[IO] {

    case req @ POST -> Root / "states" / LongVar(entityId) =>
      for {
        updateStateDTO <- req.decodeJson[EntityUpdateStateDTO]
        etNewState     <- entityStateTransitionService.transition(entityId, updateStateDTO.state).value
        response <- etNewState match {
          case Left(value) =>
            BadRequest(
              value.asJson
            )

          case Right(value) =>
            Created(
              value.asJson
            )
        }

      } yield response
  }
}
