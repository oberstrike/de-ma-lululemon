package de.ma.lululemon.jobs.pages.common

import de.ma.lululemon.jobs.pages.lululemon.ArticleSize

interface IProductPage {
    fun price(): Float
    fun size(sizeName: String): ArticleSize
    fun isPageNotFound(): Boolean
}