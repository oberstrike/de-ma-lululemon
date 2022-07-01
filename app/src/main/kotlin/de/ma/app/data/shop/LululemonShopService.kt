package de.ma.app.data.shop

import de.ma.tracker.domain.product.message.ProductShow
import de.ma.tracker.domain.shop.ShopService
import de.ma.tracker.domain.state.message.StateCreate
import javax.enterprise.context.ApplicationScoped

@ApplicationScoped
class LululemonShopService: ShopService {

    override val name: String = "lululemon"

    override fun track(productShow: ProductShow): StateCreate {
        TODO("Not yet implemented")
    }
}