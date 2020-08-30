package db.dto

import io.circe.{Decoder, Encoder}
import db.dto.Codecs._

final case class TransitionState(name: String) extends AnyVal

final case class TransitionMatrix(
    id: Long,
    from_state: TransitionState,
    possible_next_states: Set[TransitionState]
)

final case class StateTransitionError(msg: String)

object TransitionMatrix {
  val DEFAULT_TRANSITION_FROM_STATE = TransitionState("init")

  implicit val encodeTransitionMatrix: Encoder[TransitionMatrix] =
    Encoder.forProduct3("id", "from_state", "possible_next_states")(tm =>
      (tm.id, tm.from_state, tm.possible_next_states.map(_.name))
    )
  implicit val decodeTransitionMatrix: Decoder[TransitionMatrix] =
    Decoder.forProduct3("id", "from_state", "possible_next_states")(TransitionMatrix.apply)
}
