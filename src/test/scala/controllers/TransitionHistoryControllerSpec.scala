package controllers

import cats.effect.IO
import db.dto.{TransitionHistory, TransitionState}
import io.circe.Json
import io.circe.literal._
import org.http4s.circe._
import org.http4s.dsl.io._
import org.http4s.implicits._
import org.http4s.{Request, Response, Status}
import org.scalamock.scalatest.MockFactory
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import repository.TransitionHistoryRepository
import db.dto.Codecs._

class TransitionHistoryControllerSpec extends AnyWordSpec with MockFactory with Matchers {
  private val repository = stub[TransitionHistoryRepository]

  private val service = new TransitionHistoryController(repository).routes

  "TransitionHistoryController" should {
    "return all Entities" in {
      val t1 = TransitionHistory(1, 1, TransitionState("from1"), TransitionState("to1"))
      val t2 = TransitionHistory(2, 1, TransitionState("from2"), TransitionState("to2"))

      val entities = List(t2, t1)

      (repository.getTransitionHistories _).when().returns(IO.pure(entities))

      val response = serve(Request[IO](GET, uri"/transitions"))
      response.status shouldBe Status.Ok
      response.as[Json].unsafeRunSync() shouldBe
        json"""
            [
             {
              "id": ${t2.id},
              "entity_id": ${t2.entity_id},
              "from_state": ${t2.from_state},
              "to_state": ${t2.to_state}
             },
             {
               "id": ${t1.id},
               "entity_id": ${t1.entity_id},
               "from_state": ${t1.from_state},
               "to_state": ${t1.to_state}
             }
            ]"""
    }
  }

  private def serve(request: Request[IO]): Response[IO] = {
    service.orNotFound(request).unsafeRunSync()
  }
}
