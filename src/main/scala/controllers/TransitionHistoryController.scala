package controllers

import cats.effect.IO
import io.circe.syntax._
import org.http4s.HttpRoutes
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl
import repository.TransitionHistoryRepository

class TransitionHistoryController(repository: TransitionHistoryRepository) extends Http4sDsl[IO] {

  val routes: HttpRoutes[IO] = HttpRoutes.of[IO] {
    case GET -> Root / "transitions" =>
      Ok(
        repository.getTransitionHistories.map(_.asJson)
      )
  }
}
