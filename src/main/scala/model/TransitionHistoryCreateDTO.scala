package model

import db.dto.TransitionState

final case class TransitionHistoryCreateDTO(
    entity_id: Long,
    from_state: TransitionState,
    to_state: TransitionState
)
