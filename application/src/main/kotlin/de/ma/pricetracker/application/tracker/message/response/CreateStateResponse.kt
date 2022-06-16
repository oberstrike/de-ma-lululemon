package de.ma.pricetracker.application.tracker.message.response

import de.ma.tracker.domain.product.entity.Product
import de.ma.tracker.domain.product.entity.State

class CreateStateResponse(
    state: State
) {
    var id: Long? = state.id

    var productId: Long? = state.product.id

    var searchCount: Long = state.searchCount
}