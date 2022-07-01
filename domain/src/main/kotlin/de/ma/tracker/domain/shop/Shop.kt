package de.ma.tracker.domain.shop

import de.ma.tracker.domain.base.IBaseEntity
import de.ma.tracker.domain.product.Product

interface Shop : IBaseEntity{
    var name: String
    val products: Set<Product>
}