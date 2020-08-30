package controllers

import cats.effect.IO
import io.circe.syntax._
import model.TransitionMatrixCreateDTO
import org.http4s.HttpRoutes
import org.http4s.circe._
import services.TransitionMatrixService

class TransitionMatricesController(transitionMatrixService: TransitionMatrixService)
    extends BaseController {

  val routes: HttpRoutes[IO] = HttpRoutes.of[IO] {
    case GET -> Root / "transition-matrices" =>
      Ok(
        transitionMatrixService.getTransitionMatrixList.map(_.asJson)
      )

    case req @ POST -> Root / "transition-matrices" =>
      for {
        transitionMatrixCreateDTO <- req.decodeJson[TransitionMatrixCreateDTO]
        response <- toResponseCreated(
          transitionMatrixService.createTransitionMatrix(transitionMatrixCreateDTO)
        )
      } yield response

    case DELETE -> Root / "transition-matrices" / LongVar(id) =>
      toResponseNoContent(transitionMatrixService.deleteTransitionMatrix(id))
  }
}
