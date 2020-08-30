package model

import db.dto.TransitionState

final case class TransitionMatrixCreateDTO(
    from_state: TransitionState,
    possible_next_states: Set[TransitionState]
)
