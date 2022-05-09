package de.ma.lululemon.jobs.lululemon.product

data class ProductPageModel(
    val product: Product,
)

data class Product(
    val colorGroups: List<ColorGroup>,
    val sizes: List<Size>
)

data class ColorGroup(
    val colors: List<String>,
    val price: Float
)

data class Size(
    val name: String,
    val available: Boolean
)