package de.ma.app.data.shop

import de.ma.tracker.domain.shop.ShopService
import de.ma.tracker.domain.shop.ShopServiceGateway
import java.util.*
import javax.enterprise.context.ApplicationScoped
import javax.enterprise.inject.Instance

@ApplicationScoped
class ShopServiceGatewayImpl(
    private val instances: Instance<ShopService>
) : ShopServiceGateway {

    override fun getAllShops(): List<ShopService> {
        return instances.toList()
    }

    override fun getShopServiceById(id: UUID): ShopService {
        return instances.find { it.shop.id == id } ?: throw IllegalStateException("")
    }
}