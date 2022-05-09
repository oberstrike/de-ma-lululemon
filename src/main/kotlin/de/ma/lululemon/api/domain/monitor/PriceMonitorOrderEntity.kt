package de.ma.lululemon.api.domain.monitor

import de.ma.lululemon.api.domain.product.ProductEntity
import io.quarkus.mongodb.panache.common.MongoEntity
import io.quarkus.mongodb.panache.kotlin.PanacheMongoEntity
import java.time.LocalDateTime


@MongoEntity
data class PriceMonitorOrderEntity(
    val product: ProductEntity,
    val searchCount: Long = 0,
    val startDateTime: LocalDateTime = LocalDateTime.now()
) : PanacheMongoEntity()