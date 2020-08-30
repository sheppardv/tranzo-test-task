package controllers

import cats.effect.IO
import io.circe.generic.auto._
import io.circe.syntax._
import model.EntityCreateDTO
import db.dto.Entity._
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl
import org.http4s.HttpRoutes
import repository.EntityRepository

class EntityController(repository: EntityRepository) extends Http4sDsl[IO] {

  val routes: HttpRoutes[IO] = HttpRoutes.of[IO] {
    case GET -> Root / "entities" =>
      Ok(
        repository.getEntities.map(_.asJson)
      )

    case req @ POST -> Root / "entities" =>
      for {
        entityCreateDTO <- req.decodeJson[EntityCreateDTO]
        createdEntity   <- repository.createEntity(entityCreateDTO)
        response <- Created(
          createdEntity.asJson
        )
      } yield response
  }
}
