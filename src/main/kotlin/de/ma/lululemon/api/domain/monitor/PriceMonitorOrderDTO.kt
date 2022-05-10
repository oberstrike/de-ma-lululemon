package de.ma.lululemon.api.domain.monitor

import java.time.LocalDateTime

data class PriceMonitorOrderDTO(
    val product: Product,
    val searchCount: Long,
    val startDateTime: LocalDateTime
)

fun PriceMonitorOrderEntity.toDTO(): PriceMonitorOrderDTO {
    return PriceMonitorOrderDTO(
        product = product!!,
        searchCount = this.searchCount,
        startDateTime = this.startDateTime
    )
}