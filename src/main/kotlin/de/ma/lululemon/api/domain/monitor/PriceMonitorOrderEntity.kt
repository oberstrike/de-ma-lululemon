package de.ma.lululemon.api.domain.monitor

import de.ma.lululemon.jobs.pages.ShopType
import de.ma.lululemon.api.domain.monitor.product.Product
import io.quarkus.mongodb.panache.common.MongoEntity
import io.quarkus.mongodb.panache.kotlin.PanacheMongoEntity
import java.time.LocalDateTime


@MongoEntity
data class PriceMonitorOrderEntity(
    var product: Product = Product(),
    var searchCount: Long = 0,
    var startDateTime: LocalDateTime = LocalDateTime.now(),
    var shopType: ShopType = ShopType.LULULEMON
) : PanacheMongoEntity() {


    fun increaseSearchCount() {
        this.searchCount++
    }
}