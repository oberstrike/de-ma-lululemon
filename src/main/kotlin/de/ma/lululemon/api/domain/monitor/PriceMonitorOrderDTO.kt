package de.ma.lululemon.api.domain.monitor

import java.time.LocalDateTime

data class PriceMonitorOrderDTO(
    val id: String,
    val product: Product,
    val searchCount: Long,
    val startDateTime: LocalDateTime
)

fun PriceMonitorOrderEntity.toDTO(): PriceMonitorOrderDTO {
    return PriceMonitorOrderDTO(
        id = id!!.toHexString(),
        product = product!!,
        searchCount = this.searchCount,
        startDateTime = this.startDateTime
    )
}