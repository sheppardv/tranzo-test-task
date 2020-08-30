import cats.data.EitherT
import cats.effect.IO
import io.circe.Encoder

package object model {
  trait AppError {
    def message: String
  }

  final case class NotFoundError(message: String = "Not found") extends AppError

  final case class InvalidTransitionError(message: String) extends AppError

  final case class ResponseError(error: String)

  object ResponseError {
    implicit val encodeAppError: Encoder[AppError] = Encoder.forProduct1("error")(_.message)
  }

  type AppResp[A] = EitherT[IO, AppError, A]
}
