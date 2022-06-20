package de.ma.tracker.domain.product.message

import java.util.*

interface ProductShow {

    var id: UUID

    var pId: String?

    var color: String

    var size: String

    var name: String

    var version: Long

    var states: List<UUID>?
}