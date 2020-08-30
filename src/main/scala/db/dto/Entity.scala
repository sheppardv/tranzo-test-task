package db.dto

import io.circe.{Decoder, Encoder}

final case class Entity(id: Long, name: String)

object Entity {
  implicit val encodeEntity: Encoder[Entity] =
    Encoder.forProduct2("id", "name")(e => (e.id, e.name))

  implicit val decodeEntity: Decoder[Entity] =
    Decoder.forProduct2("id", "name")(Entity.apply)
}
