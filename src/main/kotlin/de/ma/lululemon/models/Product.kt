package de.ma.lululemon.models

import io.quarkus.mongodb.panache.kotlin.PanacheMongoEntity
import io.quarkus.mongodb.panache.kotlin.PanacheMongoEntityBase

data class Product(
    var id: String,
    var price: Double,
) : PanacheMongoEntityBase()
