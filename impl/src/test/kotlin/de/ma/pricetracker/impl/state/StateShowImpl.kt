package de.ma.pricetracker.impl.state

import de.ma.tracker.domain.state.message.StateShow
import java.time.LocalDateTime
import java.util.*

data class StateShowImpl(
    override val id: UUID,
    override val price: Float,
    override val entryDate: LocalDateTime
) : StateShow
