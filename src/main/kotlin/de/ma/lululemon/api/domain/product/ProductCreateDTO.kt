package de.ma.lululemon.api.domain.product

data class ProductCreateDTO(
    val prodId: String,
    val prodColor: String,
    val prodSize: String,
    val prodName: String
)

fun ProductCreateDTO.toEntity(): ProductEntity {
    return ProductEntity(
        prodId = prodId,
        prodColor = prodColor,
        prodSize = prodSize,
        prodName = prodName
    )
}