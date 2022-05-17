package de.ma.lululemon.jobs.pages.underarmour

import de.ma.lululemon.api.domain.monitor.product.ProductCreateDTO
import de.ma.lululemon.jobs.pages.ShopType
import de.ma.lululemon.jobs.pages.common.BaseShopService
import de.ma.lululemon.jobs.pages.common.IProductPage
import org.jsoup.nodes.Document
import javax.enterprise.context.ApplicationScoped

@ApplicationScoped
class UnderArmourService : BaseShopService() {

    override fun getProductPage(document: Document): IProductPage {
        return UnderArmourProductPage(document)
    }

    override fun isShop(shopType: ShopType): Boolean {
        return shopType == ShopType.UNDER_ARMOUR
    }


    override fun createUrl(productCreate: ProductCreateDTO): String {
        TODO("Not yet implemented")
    }
}