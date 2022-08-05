package de.ma.pricetracker.impl.state

import de.ma.tracker.domain.state.message.StateCreate
import java.time.LocalDateTime

data class StateCreateImpl(
    override val price: Float,
    override val entryDate: LocalDateTime?
) : StateCreate
