package de.ma.pricetracker.api.product

import de.ma.tracker.domain.state.message.StateCreate
import java.util.*

interface AddStateToProductUseCase {

    fun execute(productId: UUID, stateCreate: StateCreate)

}