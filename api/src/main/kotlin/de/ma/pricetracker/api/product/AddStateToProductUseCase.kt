package de.ma.pricetracker.api.product

import de.ma.tracker.domain.state.message.StateCreate
import de.ma.tracker.domain.state.message.StateShow
import java.util.*

interface AddStateToProductUseCase {

    operator fun invoke(productId: UUID, stateCreate: StateCreate): StateShow

}