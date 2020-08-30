package controllers

import cats.effect.IO
import io.circe.Encoder
import io.circe.syntax._
import model.{AppResp, NotFoundError}
import model.ResponseError.encodeAppError
import org.http4s.Response
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl

trait BaseController extends Http4sDsl[IO] {
  def toResponseOk[A: Encoder](
      controllerResult: AppResp[A]
  ): IO[Response[IO]] = {
    controllerResult.value.flatMap {
      case Left(value) =>
        value match {
          case _: NotFoundError =>
            NotFound(
              value.asJson
            )
          case _ =>
            BadRequest(
              value.asJson
            )
        }

      case Right(value) =>
        Ok(
          value.asJson
        )
    }
  }

  def toResponseNoContent[A: Encoder](
      controllerResult: AppResp[A],
  ): IO[Response[IO]] = {
    controllerResult.value.flatMap {
      case Left(value) =>
        value match {
          case _: NotFoundError =>
            NotFound(
              value.asJson
            )
          case _ =>
            BadRequest(
              value.asJson
            )
        }

      case Right(_) =>
        NoContent()
    }
  }

  def toResponseCreated[A: Encoder](
      controllerResult: AppResp[A]
  ): IO[Response[IO]] = {
    controllerResult.value.flatMap {
      case Left(value) =>
        value match {
          case _: NotFoundError =>
            NotFound(
              value.asJson
            )
          case _ =>
            BadRequest(
              value.asJson
            )
        }

      case Right(value) =>
        Created(
          value.asJson
        )
    }
  }
}
