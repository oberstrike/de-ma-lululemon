package de.ma.lululemon.jobs.pages.underarmour

import de.ma.lululemon.jobs.pages.common.IProductPage
import de.ma.lululemon.jobs.pages.lululemon.ArticleSize
import org.jsoup.nodes.Document

class UnderArmourProductPage(private val document: Document) : IProductPage {

    override fun price(): Float {
        return document.select("div.b-price").first()
            ?.text()
            ?.removeNotNumbers()
            ?.replace(",", ".")
            ?.toFloat() ?: 0f
    }

    override fun size(sizeName: String): ArticleSize {
        val sizeElement = document.select("a[data-size-attr=$sizeName]").first()
            ?: throw IllegalArgumentException("Color $sizeName not found")
        val isDisabled = sizeElement.hasClass("disabled")
        return ArticleSize(sizeName, !isDisabled)
    }

    override fun isPageNotFound(): Boolean {
        return document.select("div.error-page-wrapper").firstOrNull() != null
    }

    //removes all letters but numbers and commas
    private fun String.removeNotNumbers(): String {
        return this.replace(Regex("[^0-9,]"), "")
    }

}