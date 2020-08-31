package controllers

import cats.effect.IO
import org.http4s.{Headers, MediaType, Response, Status}
import org.http4s.headers.`Content-Type`
import fs2.Stream
import fs2.text.utf8Encode

object Helpers {
  val jsonNotFound: Response[IO] =
    Response(
      Status.NotFound,
      body = Stream("""{"error": "Not found"}""").through(utf8Encode),
      headers = Headers(`Content-Type`(MediaType.application.json) :: Nil)
    )
}
