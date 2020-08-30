package db.dto

import io.circe.{Decoder, Encoder}

object Codecs {
  implicit val encodeTransitionState: Encoder[TransitionState] =
    Encoder.encodeString.contramap[TransitionState](_.name)

  implicit val decodeTransitionState: Decoder[TransitionState] =
    Decoder.decodeString.map(TransitionState)
}
