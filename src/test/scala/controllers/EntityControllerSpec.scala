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

  private val entityController = new EntityController(repository).routes

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
  }

  private def serve(request: Request[IO]): Response[IO] = {
    entityController.orNotFound(request).unsafeRunSync()
  }
}
