package db.dto

import io.circe.{Decoder, Encoder}
import db.dto.Codecs._

final case class TransitionHistory(
    id: Long,
    entity_id: Long,
    from_state: TransitionState,
    to_state: TransitionState
)

object TransitionHistory {
  implicit val encodeTransitionHistory: Encoder[TransitionHistory] =
    Encoder.forProduct4("id", "entity_id", "from_state", "to_state")(tm =>
      (tm.id, tm.entity_id, tm.from_state, tm.to_state)
    )

  implicit val decodeTransitionHistory: Decoder[TransitionHistory] =
    Decoder.forProduct4("id", "entity_id", "from_state", "to_state")(TransitionHistory.apply)
}
