package de.ma.tracker.domain.product.message

interface ProductUpdate {

    var pId: String?

    var color: String

    var size: String

    var name: String

    var version: Long
}