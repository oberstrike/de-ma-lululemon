package de.ma.lululemon.api.domain.monitor

import java.time.LocalDateTime

data class PriceMonitorOrderDTO(
    val prodId: String,
    val searchCount: Long,
    val startDateTime: LocalDateTime
)

fun PriceMonitorOrderEntity.toDTO(): PriceMonitorOrderDTO {
    return PriceMonitorOrderDTO(
        prodId = this.product.id?.toHexString() ?: "",
        searchCount = this.searchCount,
        startDateTime = this.startDateTime
    )
}