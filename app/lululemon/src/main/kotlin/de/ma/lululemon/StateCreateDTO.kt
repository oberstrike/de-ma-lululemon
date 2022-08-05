package de.ma.lululemon

import de.ma.tracker.domain.state.message.StateCreate
import java.time.LocalDateTime

data class StateCreateDTO(
    override val entryDate: LocalDateTime?,
    override val price: Float
) : StateCreate
