package repository

import cats.effect.IO
import db.dto.Entity
import doobie.implicits._
import doobie.util.transactor.Transactor
import model.{EntityCreateDTO, NotFoundError}

class EntityRepository(transactor: Transactor[IO]) {
  def getEntities: IO[List[Entity]] = {
    sql"SELECT id, name FROM entity"
      .query[Entity]
      .stream
      .compile
      .toList
      .transact(transactor)
  }

  def getEntity(id: Long): IO[Either[NotFoundError, Entity]] = {
    sql"SELECT id, name FROM entity WHERE id = $id".query[Entity].option.transact(transactor).map {
      case Some(todo) => Right(todo)
      case None       => Left(NotFoundError())
    }
  }

  def getEntityByIdUnique(id: Long): IO[Entity] = {
    sql"SELECT id, name FROM entity WHERE id=$id"
      .query[Entity]
      .unique
      .transact(transactor)
  }

  def createEntity(entityCreateDTO: EntityCreateDTO): IO[Entity] = {
    sql"INSERT INTO entity (name) VALUES (${entityCreateDTO.name})".update
      .withUniqueGeneratedKeys[Long]("id")
      .transact(transactor)
      .flatMap(getEntityByIdUnique)
  }
}
