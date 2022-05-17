package de.ma.lululemon.jobs.pages.lululemon

import de.ma.lululemon.api.domain.monitor.product.ProductCreateDTO
import de.ma.lululemon.api.domain.monitor.product.ProductDTO
import de.ma.lululemon.jobs.pages.common.IShopService
import de.ma.lululemon.jobs.pages.ShopType
import de.ma.lululemon.api.domain.monitor.product.State
import de.ma.lululemon.jobs.pages.common.BaseShopService
import de.ma.lululemon.jobs.pages.common.IProductPage
import org.jsoup.nodes.Document
import java.time.LocalDateTime
import javax.enterprise.context.ApplicationScoped

@ApplicationScoped
class LululemonService(
    private val lululemonUrlGenerator: LululemonUrlGenerator
) : BaseShopService() {

    override fun isShop(shopType: ShopType): Boolean {
        return shopType == ShopType.LULULEMON
    }

    override fun getProductPage(document: Document): IProductPage {
        return LululemonProductPage(document)
    }

    override fun createUrl(productCreate: ProductCreateDTO): String {
        return lululemonUrlGenerator.createUrl(productCreate)
    }

}