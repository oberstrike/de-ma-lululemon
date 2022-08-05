package de.ma.lululemon


interface IProductPage {
    fun price(): Float
    fun size(sizeName: String): ArticleSize
    fun isPageNotFound(): Boolean
}

data class Color(
    val name: String,
    val selected: Boolean
)

data class ArticleSize(
    val name: String,
    val available: Boolean
)