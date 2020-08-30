package controllers

import cats.effect.IO
import io.circe.generic.auto._
import io.circe.syntax._
import model.{EntityCreateDTO, NotFoundError}
import db.dto.Entity._
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl
import org.http4s.headers.{Location, `Content-Type`}
import org.http4s.{HttpRoutes, Uri}
import repository.EntityRepository

class EntityController(repository: EntityRepository) extends Http4sDsl[IO] {

  val routes: HttpRoutes[IO] = HttpRoutes.of[IO] {
    case GET -> Root / "entities" =>
      Ok(
        repository.getEntities.map(_.asJson)
      )
//
//    case GET -> Root / "todos" / LongVar(id) =>
//      for {
//        getResult <- repository.getTodo(id)
//        response <- todoResult(getResult)
//      } yield response
//
    case req @ POST -> Root / "entities" =>
      for {
        entityCreateDTO <- req.decodeJson[EntityCreateDTO]
        createdTodo     <- repository.createEntity(entityCreateDTO)
        response <- Created(
          createdTodo.asJson,
          Location(Uri.unsafeFromString(s"/entities/${createdTodo.id}"))
        )
      } yield response
//
//    case req @ PUT -> Root / "todos" / LongVar(id) =>
//      for {
//        todo <-req.decodeJson[Todo]
//        updateResult <- repository.updateTodo(id, todo)
//        response <- todoResult(updateResult)
//      } yield response
//
//    case DELETE -> Root / "todos" / LongVar(id) =>
//      repository.deleteTodo(id).flatMap {
//        case Left(TodoNotFoundError) => NotFound()
//        case Right(_) => NoContent()
//      }
  }

//  private def todoResult(result: Either[TodoNotFoundError.type, Todo]) = {
//    result match {
//      case Left(TodoNotFoundError) => NotFound()
//      case Right(todo) => Ok(todo.asJson)
//    }
//  }
}
