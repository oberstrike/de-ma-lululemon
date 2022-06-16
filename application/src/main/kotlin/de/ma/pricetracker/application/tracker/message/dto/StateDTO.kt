package de.ma.pricetracker.application.tracker.message.dto

import de.ma.tracker.domain.product.entity.Product

data class StateDTO(
    val product: Long,
    val searchCount: Long
)