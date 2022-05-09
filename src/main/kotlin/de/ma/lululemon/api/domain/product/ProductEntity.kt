package de.ma.lululemon.api.domain.product

import io.quarkus.mongodb.panache.common.MongoEntity
import io.quarkus.mongodb.panache.kotlin.PanacheMongoEntity
import java.time.LocalDateTime


@MongoEntity
data class ProductEntity(
    val prodId: String,
    val prodColor: String,
    val prodSize: String,
    val prodName: String,
    val entries: MutableSet<Entry> = mutableSetOf()
) : PanacheMongoEntity() {

    fun addEntry(entry: Entry) {
        this.entries.add(entry)
    }
}

data class Entry(
    val price: Float,
    val timestamp: LocalDateTime
)
