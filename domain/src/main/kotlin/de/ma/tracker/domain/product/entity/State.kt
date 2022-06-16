package de.ma.tracker.domain.product.entity

import de.ma.tracker.domain.base.IBaseEntity

interface State : IBaseEntity {
    var product: Product?
    var searchCount: Long
    var version: Long
}