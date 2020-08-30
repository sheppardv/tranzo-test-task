package model

import db.dto.TransitionState
import io.circe.Decoder
import db.dto.Codecs._

final case class TransitionMatrixCreateDTO(
    from_state: TransitionState,
    possible_next_states: Set[TransitionState]
)

object TransitionMatrixCreateDTO {
  implicit val decodeTransitionMatrixCreateDTO: Decoder[TransitionMatrixCreateDTO] =
    Decoder.forProduct2("from_state", "possible_next_states")(TransitionMatrixCreateDTO.apply)
}
