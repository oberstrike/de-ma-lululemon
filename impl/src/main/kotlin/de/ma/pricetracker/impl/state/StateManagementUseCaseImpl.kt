package de.ma.pricetracker.impl.state

import de.ma.pricetracker.api.state.StateManagementUseCase
import de.ma.tracker.domain.state.StateGateway
import de.ma.tracker.domain.state.message.StateShow
import de.ma.tracker.domain.state.message.StateUpdate
import java.util.*

class StateManagementUseCaseImpl(
    private val stateGateway: StateGateway
) : StateManagementUseCase {


    override fun updateStateById(stateUpdate: StateUpdate, stateId: UUID): StateShow {
        val existsById = stateGateway.existsById(stateId)

        if (!existsById) {
            throw NoSuchElementException("No State with id $stateId found.")
        }

        return stateGateway.updateState(stateUpdate, stateId)
    }

    override fun deleteStateById(stateId: UUID) {
        val existsById = stateGateway.existsById(stateId)

        if (!existsById) {
            throw NoSuchElementException("No State with id $stateId found.")
        }

        stateGateway.deleteStateById(stateId)

    }

}