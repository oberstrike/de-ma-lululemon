package de.ma.lululemon.api.domain.monitor

data class ProductCreate(
    val id: String,
    val color: String,
    val size: String,
    val name: String
)

fun ProductCreate.toProduct(): Product {
    val product = Product()
    product.id = this.id
    product.color = this.color
    product.size = this.size
    product.name = this.name
    return product
}