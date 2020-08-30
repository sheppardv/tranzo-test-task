package model

import db.dto.TransitionState
import db.dto.Codecs._
import io.circe.Decoder

final case class EntityUpdateStateDTO(
    state: TransitionState
)

object EntityUpdateStateDTO {
  implicit val decodeEntityUpdateStateDTO: Decoder[EntityUpdateStateDTO] =
    Decoder.forProduct1("state")(EntityUpdateStateDTO.apply)
}
