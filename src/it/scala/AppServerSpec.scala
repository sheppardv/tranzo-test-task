import cats.effect.{ContextShift, IO, Timer}
import config.Config
import io.circe.Json
import io.circe.literal._
import io.circe.optics.JsonPath._
import org.http4s.circe._
import org.http4s.client.blaze.BlazeClientBuilder
import org.http4s.{Method, Request, Status, Uri}
import org.scalatest.BeforeAndAfterAll
import org.scalatest.concurrent.Eventually
import org.scalatest.matchers.should.Matchers
import org.scalatest.time.{Millis, Seconds, Span}
import org.scalatest.wordspec.AnyWordSpec

import scala.concurrent.ExecutionContext
import scala.concurrent.ExecutionContext.Implicits.global

class AppServerSpec extends AnyWordSpec with Matchers with BeforeAndAfterAll with Eventually {
  private implicit val timer: Timer[IO] = IO.timer(ExecutionContext.global)

  private implicit val contextShift: ContextShift[IO] = IO.contextShift(ExecutionContext.global)

  private lazy val client = BlazeClientBuilder[IO](global).resource

  private val configFile = "test.conf"

  private lazy val config = Config.load(configFile).use(config => IO.pure(config)).unsafeRunSync()

  private lazy val urlStart = s"http://${config.server.host}:${config.server.port}"

  implicit override val patienceConfig: PatienceConfig =
    PatienceConfig(timeout = scaled(Span(5, Seconds)), interval = scaled(Span(100, Millis)))

  override def beforeAll(): Unit = {
    HttpServer.create(configFile).unsafeRunAsyncAndForget()
    eventually {
      client
        .use(_.statusFromUri(Uri.unsafeFromString(s"$urlStart/entities")))
        .unsafeRunSync() shouldBe Status.Ok
    }
    ()
  }

  "App server" should {
    "create an Entity" in {
      val name       = "new entity"
      val createJson = json"""
        {
          "name": $name
        }"""
      val request =
        Request[IO](method = Method.POST, uri = Uri.unsafeFromString(s"$urlStart/entities"))
          .withEntity(createJson)

      val json = client.use(_.expect[Json](request)).unsafeRunSync()
      root.id.long.getOption(json).nonEmpty shouldBe true
      root.name.string.getOption(json) shouldBe Some(name)
    }

    "return all entities" in {
      client
        .use(_.expect[Json](Uri.unsafeFromString(s"$urlStart/entities")))
        // those are predefined, for "real" integration tests we have to create them
        .unsafeRunSync shouldBe json"""
        [
          {
            "id": 1,
            "name": "entity1"
          },
          {
            "id": 2,
            "name": "entity2"
          },
          {
            "id": 3,
            "name": "entity3"
          }
        ]"""
    }
  }
}
