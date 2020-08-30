import cats.implicits._
import cats.effect._
import config.Config
import db.Database
import doobie.hikari.HikariTransactor
import doobie.util.ExecutionContexts
import org.http4s.implicits._
import org.http4s.server.blaze.BlazeServerBuilder
import repository.{EntityRepository, TransitionHistoryRepository, TransitionMatrixRepository}
import controllers.{
  EntityController,
  EntityTransitionController,
  TransitionHistoryController,
  TransitionMatricesController
}
import services.{EntityStateTransitionService, TransitionMatrixService}

object HttpServer {
  def create(configFile: String = "application.conf")(implicit
      contextShift: ContextShift[IO],
      concurrentEffect: ConcurrentEffect[IO],
      timer: Timer[IO]
  ): IO[ExitCode] = {
    resources(configFile).use(create)
  }

  private def resources(
      configFile: String
  )(implicit contextShift: ContextShift[IO]): Resource[IO, Resources] = {
    for {
      config     <- Config.load(configFile)
      ec         <- ExecutionContexts.fixedThreadPool[IO](config.database.threadPoolSize)
      blocker    <- Blocker[IO]
      transactor <- Database.transactor(config.database, ec, blocker)
    } yield Resources(transactor, config)
  }

  private def create(
      resources: Resources
  )(implicit concurrentEffect: ConcurrentEffect[IO], timer: Timer[IO]): IO[ExitCode] = {
    for {
      _ <- Database.initialize(resources.transactor)

      entityRepository            = new EntityRepository(resources.transactor)
      transitionMatrixRepository  = new TransitionMatrixRepository(resources.transactor)
      transitionHistoryRepository = new TransitionHistoryRepository(resources.transactor)

      entityStateTransitionService = new EntityStateTransitionService(
        entityRepository = entityRepository,
        transitionMatrixRepository = transitionMatrixRepository,
        transitionHistoryRepository = transitionHistoryRepository
      )

      transitionMatrixService = new TransitionMatrixService(transitionMatrixRepository)

      entityController             = new EntityController(entityRepository)
      transitionMatricesController = new TransitionMatricesController(transitionMatrixService)
      transitionHistoryController  = new TransitionHistoryController(transitionHistoryRepository)
      entityTransitionController   = new EntityTransitionController(entityStateTransitionService)

      composedRoutes =
        entityController.routes <+>
          transitionMatricesController.routes <+>
          transitionHistoryController.routes <+>
          entityTransitionController.routes

      exitCode <-
        BlazeServerBuilder[IO]
          .bindHttp(resources.config.server.port, resources.config.server.host)
          .withHttpApp(
            composedRoutes.orNotFound
          )
          .serve
          .compile
          .lastOrError
    } yield exitCode
  }

  case class Resources(transactor: HikariTransactor[IO], config: Config)

}
