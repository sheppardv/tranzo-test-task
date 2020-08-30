package controllers

import cats.effect.IO
import db.dto.Entity._
import io.circe.syntax._
import org.http4s.HttpRoutes
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl
import repository.TransitionMatrixRepository

class TransitionMatricesController(repository: TransitionMatrixRepository) extends Http4sDsl[IO] {

  val routes: HttpRoutes[IO] = HttpRoutes.of[IO] {
    case GET -> Root / "transition-matrices" =>
      Ok(
        repository.getTransitionMatrices.map(_.asJson)
      )
  }
}
