package de.ma.pricetracker.application.tracker.message.response

import de.ma.tracker.domain.product.entity.State

class GetStateResponse(state: State) {

    private val id = state.id

    private val productId = state.product.id

    private val searchCount = state.searchCount
}