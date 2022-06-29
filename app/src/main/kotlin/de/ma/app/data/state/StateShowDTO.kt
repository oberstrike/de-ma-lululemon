package de.ma.app.data.state

import de.ma.tracker.domain.state.message.StateShow
import java.time.LocalDateTime
import java.util.*

data class StateShowDTO(
    override val price: Long,
    override val id: UUID,
    override val entryDate: LocalDateTime
) : StateShow