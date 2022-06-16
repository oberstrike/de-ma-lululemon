package de.ma.pricetracker.application.tracker.converter

import de.ma.pricetracker.application.tracker.message.request.CreateStateRequest
import de.ma.tracker.domain.product.entity.State

interface StateConverter {
    fun convert(createStateRequest: CreateStateRequest): State
}