package de.ma.app.infrastructure.rest.state.data

import de.ma.tracker.domain.state.message.StateUpdate
import java.time.LocalDateTime

data class StateUpdateDTO(
    override val entryDate: LocalDateTime,
    override val price: Float
): StateUpdate
