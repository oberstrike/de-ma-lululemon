package de.ma.app.data.state

import de.ma.tracker.domain.state.StateGateway
import de.ma.tracker.domain.state.message.StateShow
import de.ma.tracker.domain.state.message.StateUpdate
import java.util.*
import javax.enterprise.context.ApplicationScoped
import javax.transaction.Transactional

@ApplicationScoped
class StateGatewayImpl(
    private val stateRepository: StateRepository
) : StateGateway {

    override fun deleteStateById(id: UUID) {
        stateRepository.deleteById(id)
    }

    override fun existsById(id: UUID): Boolean {
        return stateRepository.findByIdOptional(id).isPresent
    }

    @Transactional
    override fun updateState(stateUpdate: StateUpdate, id: UUID): StateShow {
        val entity = stateRepository.findByIdProductFetched(id)
        entity.price = stateUpdate.price
        entity.entryDate = stateUpdate.entryDate

        val entityManager = stateRepository.entityManager

        entityManager.merge(entity)
        return entity.toShow()
    }

}