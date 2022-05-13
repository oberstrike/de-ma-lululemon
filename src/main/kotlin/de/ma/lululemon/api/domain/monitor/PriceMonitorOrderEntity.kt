package de.ma.lululemon.api.domain.monitor

import io.quarkus.mongodb.panache.common.MongoEntity
import io.quarkus.mongodb.panache.kotlin.PanacheMongoEntity
import java.time.LocalDateTime


@MongoEntity
data class PriceMonitorOrderEntity(
    var product: Product? = null,
    var searchCount: Long = 0,
    var startDateTime: LocalDateTime = LocalDateTime.now(),
    var shopName: String = ""
) : PanacheMongoEntity() {


    fun increaseSearchCount() {
        this.searchCount++
    }
}