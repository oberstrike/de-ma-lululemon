package de.ma.tracker.domain.product.message

import java.util.*

interface ProductShow {

    val id: UUID

    val pId: String?

    val color: String

    val size: String

    val name: String

    val version: Long

    val states: List<UUID>?

}