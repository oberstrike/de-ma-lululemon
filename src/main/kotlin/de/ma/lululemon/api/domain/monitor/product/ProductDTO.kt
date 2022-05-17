package de.ma.lululemon.api.domain.monitor.product

data class ProductDTO(
    val id: String,
    val color: String,
    val size: String,
    val name: String,
    val url: String
)

fun ProductDTO.toProduct(): Product {
    val product = Product()
    product.id = this.id
    product.color = this.color
    product.size = this.size
    product.name = this.name
    product.url = this.url
    return product
}


fun Product.toDTO(): ProductDTO {
    return ProductDTO(
        this.id,
        this.color,
        this.size,
        this.name,
        this.url
    )
}