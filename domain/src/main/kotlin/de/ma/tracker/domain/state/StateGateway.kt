package de.ma.tracker.domain.state

import de.ma.tracker.domain.state.message.StateShow
import de.ma.tracker.domain.state.message.StateUpdate
import java.util.*

interface StateGateway {

    fun updateState(stateUpdate: StateUpdate, id: UUID): StateShow

    fun deleteStateById(id: UUID)

    fun existsById(id: UUID): Boolean

}