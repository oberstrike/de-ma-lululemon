package de.ma.lululemon.jobs.pages.common

import de.ma.lululemon.api.domain.monitor.product.ProductDTO
import de.ma.lululemon.api.domain.monitor.product.State
import de.ma.lululemon.jobs.pages.lululemon.LululemonProductPage
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.time.LocalDateTime

abstract class BaseShopService : IShopService {

    abstract fun getProductPage(document: Document): IProductPage

    override fun getCurrentStateOfProduct(product: ProductDTO): State? {
        val url = product.url

        val document = Jsoup.connect(url).get()

        val productPage = getProductPage(document)

        val isPageNotFound = productPage.isPageNotFound()
        if (isPageNotFound) {
            return null
        }

        val size = productPage.size(product.size)
        val price = productPage.price()

        return State().apply {
            this.price = price
            this.available = size.available
            this.timestamp = LocalDateTime.now()
        }

    }
}