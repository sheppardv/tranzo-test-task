package controllers

import cats.effect.IO
import model.EntityUpdateStateDTO
import org.http4s.HttpRoutes
import org.http4s.circe._
import services.EntityStateTransitionService

class EntityTransitionController(entityStateTransitionService: EntityStateTransitionService)
    extends BaseController {

  val routes: HttpRoutes[IO] = HttpRoutes.of[IO] {

    case req @ POST -> Root / "states" / LongVar(entityId) =>
      for {
        updateStateDTO <- req.decodeJson[EntityUpdateStateDTO]
        response     <- toResponseOk(entityStateTransitionService.transition(entityId, updateStateDTO.state))
      } yield response
  }
}
