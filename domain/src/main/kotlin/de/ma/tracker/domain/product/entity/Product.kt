package de.ma.tracker.domain.product.entity

import de.ma.tracker.domain.base.IBaseEntity
import de.ma.tracker.domain.product.vo.UrlVO


interface Product : IBaseEntity {

    var color: String

    var size: String

    var name: String

    var url: UrlVO

    var version: Long

    val states: Set<State>
}