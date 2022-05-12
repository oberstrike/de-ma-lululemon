package de.ma.lululemon.api.domain.monitor

data class ProductCreateDTO(
    val prodId: String,
    val prodColor: String,
    val prodSize: String,
    val prodName: String
)

fun ProductCreateDTO.toProduct(): Product {
    val product = Product()
    product.id = this.prodId
    product.color = this.prodColor
    product.size = this.prodSize
    product.name = this.prodName
    return product
}