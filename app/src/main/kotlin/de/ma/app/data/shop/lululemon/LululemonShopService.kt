package de.ma.app.data.shop.lululemon

import de.ma.app.data.shop.ShopImpl
import de.ma.lululemon.LululemonService
import de.ma.tracker.domain.product.message.ProductOverview
import de.ma.tracker.domain.product.message.ProductShow
import de.ma.tracker.domain.shop.Shop
import de.ma.tracker.domain.shop.ShopService
import de.ma.tracker.domain.state.message.StateCreate
import java.util.*
import javax.enterprise.context.ApplicationScoped

@ApplicationScoped
class LululemonShopService : ShopService {

    val lululemonService = LululemonService()

    override val shop: Shop = ShopImpl(
        UUID.randomUUID(),
        "Lululemon"
    )

    override fun track(productShow: ProductOverview): StateCreate {

        return lululemonService.createState(
            ProductParamImpl(
                productShow.pId ?: throw IllegalArgumentException("No product id given."),
                productShow.color,
                productShow.size
            )
        )
    }


}