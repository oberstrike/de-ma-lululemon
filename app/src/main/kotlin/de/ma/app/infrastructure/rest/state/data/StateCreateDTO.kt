package de.ma.app.infrastructure.rest.state.data

import de.ma.tracker.domain.state.message.StateCreate
import java.time.LocalDateTime

data class StateCreateDTO(
    override val price: Long,
    override val entryDate: LocalDateTime = LocalDateTime.now()
) : StateCreate
