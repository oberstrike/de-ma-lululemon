package de.ma.tracker.domain.state

import de.ma.tracker.domain.base.IBaseEntity
import de.ma.tracker.domain.product.Product
import java.time.LocalDateTime

interface State : IBaseEntity {
    var price: Long
    val product: Product?
    var entryDate: LocalDateTime
}