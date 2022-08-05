package de.ma.app.infrastructure.rest.state.data

import de.ma.tracker.domain.state.message.StateCreate
import java.time.LocalDateTime

data class StateCreateDTO(
    override val price: Float,
    override val entryDate: LocalDateTime = LocalDateTime.now()
) : StateCreate
