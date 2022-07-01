package de.ma.app.data.shop

import de.ma.tracker.domain.shop.Shop
import de.ma.tracker.domain.shop.ShopService
import de.ma.tracker.domain.shop.ShopGateway
import java.util.*
import javax.enterprise.context.ApplicationScoped
import javax.enterprise.inject.Instance

@ApplicationScoped
class ShopGatewayImpl(
    private val shopRepository: ShopRepository,
    private val shops: Instance<ShopService>
) : ShopGateway {

    override fun getShops(): List<Shop> {
        return shopRepository.list<ShopEntity>()
    }

    override fun getById(id: UUID): Shop? {
        TODO("Not yet implemented")
    }

    override fun findShopByProductId(productId: UUID): ShopService {
        val shopEntity = shopRepository.findByProductId(productId)
        return shops.findLast { it.name == shopEntity!!.name }?: throw IllegalArgumentException("No shop with the name ${shopEntity!!.name} was found")

    }
}