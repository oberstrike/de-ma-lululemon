package de.ma.lululemon.jobs.pages.lululemon

import de.ma.lululemon.api.domain.entry.IProductProcessor
import de.ma.lululemon.api.domain.monitor.Product
import de.ma.lululemon.jobs.pages.IPageModel
import de.ma.lululemon.jobs.pages.PageModel
import org.jsoup.Jsoup

class LululemonProcessor : IProductProcessor {

    override fun process(product: Product): IPageModel? {

        val url = product.url
        val document = Jsoup.connect(url).get()

        val lululemonProductPage = LululemonProductPage(document)

        val isPageNotFound = lululemonProductPage.pageNotFound()
        if (isPageNotFound) {
            return null
        }

        val productPageModel = lululemonProductPage.productPageModel()

        val selectedColorGroup = if (product.color.isEmpty()) {
            productPageModel.colorGroups.first()
        } else {
            productPageModel
                .colorGroups.first { colorGroup -> colorGroup.colors.any { it.name == product.color } }
        }

        val size = productPageModel.sizes
            .first { size -> size.name == product.size }

        return PageModel(
            selectedColorGroup.price,
            size.available
        )
    }


    override fun isShop(shopName: String): Boolean {
        return shopName == "lululemon.de"
    }
}