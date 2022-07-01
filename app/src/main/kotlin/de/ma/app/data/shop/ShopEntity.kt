package de.ma.app.data.shop

import de.ma.app.data.base.IEntity
import de.ma.app.data.base.IEntityImpl
import de.ma.app.data.product.ProductEntity
import de.ma.tracker.domain.shop.Shop
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.JoinColumn
import javax.persistence.JoinTable
import javax.persistence.OneToMany

@Entity(name = "shop")
class ShopEntity : Shop, IEntity by IEntityImpl() {

    @get:OneToMany
    @get:JoinTable(
        name = "shop_products",
        joinColumns = [JoinColumn(name = "shop_id")],
        inverseJoinColumns = [JoinColumn(name = "product_id")]
    )
    override var products: MutableSet<ProductEntity> = mutableSetOf()

    @get:Column(name = "name")
    override var name: String = ""


}