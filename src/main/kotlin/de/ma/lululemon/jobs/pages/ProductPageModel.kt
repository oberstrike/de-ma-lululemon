package de.ma.lululemon.jobs.pages

data class ProductPageModel(
    val colorGroups: List<ColorGroup>,
    val sizes: List<Size>
)

data class ColorGroup(
    val colors: List<Color>,
    val price: Float
)

data class Color(
    val name: String,
    val selected: Boolean
)

data class Size(
    val name: String,
    val available: Boolean
)