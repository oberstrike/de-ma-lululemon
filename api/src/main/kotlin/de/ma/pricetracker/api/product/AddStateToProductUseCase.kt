package de.ma.pricetracker.api.product

import de.ma.tracker.domain.state.message.StateCreate
import de.ma.tracker.domain.state.message.StateShow
import java.util.*

interface AddStateToProductUseCase {

    fun execute(productId: UUID, stateCreate: StateCreate): StateShow

}