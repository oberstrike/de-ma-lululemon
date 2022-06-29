package de.ma.pricetracker.api.state

import de.ma.tracker.domain.state.message.StateShow
import de.ma.tracker.domain.state.message.StateUpdate
import java.util.*

interface StateManagementUseCase {

    fun updateStateById(stateUpdate: StateUpdate, stateId: UUID): StateShow

    fun deleteStateById(stateId: UUID)

}