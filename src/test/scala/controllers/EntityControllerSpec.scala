package controllers

import cats.effect.IO
import db.dto.Entity
import io.circe.Json
import io.circe.literal._
import model.EntityCreateDTO
import org.http4s.circe._
import org.http4s.dsl.io._
import org.http4s.implicits._
import org.http4s.{Request, Response, Status, Uri, _}
import org.scalamock.scalatest.MockFactory
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import repository.EntityRepository

class EntityControllerSpec extends AnyWordSpec with MockFactory with Matchers {
  private val repository = stub[EntityRepository]

  private val service = new EntityController(repository).routes

  "EntityController" should {
    "create an Entity" in {
      val dbEntity        = Entity(1, "entity1")
      val createEntityDto = EntityCreateDTO(name = dbEntity.name)

      (repository.createEntity _).when(createEntityDto).returns(IO.pure(dbEntity))
      val createJson = json"""{
                              "name": ${createEntityDto.name}
                              }"""
      val response   = serve(Request[IO](POST, uri"/entities").withEntity(createJson))
      response.status shouldBe Status.Created
      response.as[Json].unsafeRunSync() shouldBe json"""
        {
          "id": ${dbEntity.id},
          "name": ${dbEntity.name}
        }"""
    }

    "return all Entities" in {
      val entity1 = Entity(1, "entity1")
      val entity2 = Entity(1, "entity2")

      val entities = List(entity1, entity2)


      (repository.getEntities _).when().returns(IO.pure(entities))

      val response = serve(Request[IO](GET, uri"/entities"))
      response.status shouldBe Status.Ok
      response.as[Json].unsafeRunSync() shouldBe
        json"""
            [
             {
              "id": ${entity1.id},
              "name": ${entity1.name}
             },
             {
               "id": ${entity2.id},
                "name": ${entity2.name}
             }
            ]"""
    }
//
//    "update a todo" in {
//      val id   = 1
//      val todo = Todo(None, "updated todo", Medium)
//      (repository.updateTodo _).when(id, todo).returns(IO.pure(Right(todo.copy(id = Some(id)))))
//      val updateJson = json"""
//        {
//          "description": ${todo.description},
//          "importance": ${todo.importance.value}
//        }"""
//
//      val response =
//        serve(Request[IO](PUT, Uri.unsafeFromString(s"/todos/$id")).withEntity(updateJson))
//      response.status shouldBe Status.Ok
//      response.as[Json].unsafeRunSync() shouldBe json"""
//        {
//          "id": $id,
//          "description": ${todo.description},
//          "importance": ${todo.importance.value}
//        }"""
//    }
//
//    "return a single todo" in {
//      val id   = 1
//      val todo = Todo(Some(id), "my todo", High)
//      (repository.getTodo _).when(id).returns(IO.pure(Right(todo)))
//
//      val response = serve(Request[IO](GET, Uri.unsafeFromString(s"/todos/$id")))
//      response.status shouldBe Status.Ok
//      response.as[Json].unsafeRunSync() shouldBe json"""
//        {
//          "id": $id,
//          "description": ${todo.description},
//          "importance": ${todo.importance.value}
//        }"""
//    }
//
//    "return all todos" in {
//      val id1   = 1
//      val todo1 = Todo(Some(id1), "my todo 1", High)
//      val id2   = 2
//      val todo2 = Todo(Some(id2), "my todo 2", Medium)
//      val todos = Stream(todo1, todo2)
//      (repository.getTodos _).when().returns(todos)
//
//      val response = serve(Request[IO](GET, uri"/todos"))
//      response.status shouldBe Status.Ok
//      response.as[Json].unsafeRunSync() shouldBe json"""
//        [
//         {
//           "id": $id1,
//           "description": ${todo1.description},
//           "importance": ${todo1.importance.value}
//         },
//         {
//           "id": $id2,
//           "description": ${todo2.description},
//           "importance": ${todo2.importance.value}
//         }
//        ]"""
//    }
//
//    "delete a todo" in {
//      val id = 1
//      (repository.deleteTodo _).when(id).returns(IO.pure(Right(())))
//
//      val response = serve(Request[IO](DELETE, Uri.unsafeFromString(s"/todos/$id")))
//      response.status shouldBe Status.NoContent
//    }
  }

  private def serve(request: Request[IO]): Response[IO] = {
    service.orNotFound(request).unsafeRunSync()
  }
}
