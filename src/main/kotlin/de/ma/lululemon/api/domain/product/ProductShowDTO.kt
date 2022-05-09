package de.ma.lululemon.api.domain.product

data class ProductShowDTO(
    val id: String? = null,
    val prodId: String,
    val prodColor: String,
    val prodSize: String,
    val entries: Set<Entry>
)

fun ProductEntity.toShowDTO(): ProductShowDTO {
    return ProductShowDTO(
        id = id?.toHexString(),
        prodId = prodId,
        prodColor = prodColor,
        prodSize = prodSize,
        entries = entries
    )
}
