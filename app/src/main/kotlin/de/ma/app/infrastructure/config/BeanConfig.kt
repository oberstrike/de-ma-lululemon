package de.ma.app.infrastructure.config

import de.ma.app.data.shop.ShopEntity
import de.ma.app.data.shop.ShopRepository
import de.ma.pricetracker.api.product.AddStateToProductUseCase
import de.ma.pricetracker.api.product.ProductManagementUseCase
import de.ma.pricetracker.api.shop.AddProductToShopUseCase
import de.ma.pricetracker.api.shop.GetProductsByShopIdUseCase
import de.ma.pricetracker.api.shop.GetShopsUseCase
import de.ma.pricetracker.api.shop.TrackPrizeOfProductUseCase
import de.ma.pricetracker.api.state.StateManagementUseCase
import de.ma.pricetracker.impl.product.AddStateToProductUseCaseImpl
import de.ma.pricetracker.impl.product.ProductManagementUseCaseImpl
import de.ma.pricetracker.impl.shop.AddProductToShopUseCaseImpl
import de.ma.pricetracker.impl.shop.GetProductsByShopIdUseCaseImpl
import de.ma.pricetracker.impl.shop.GetShopsUseCaseImpl
import de.ma.pricetracker.impl.shop.TrackPrizeOfProductUseCaseImpl
import de.ma.pricetracker.impl.state.StateManagementUseCaseImpl
import de.ma.tracker.domain.product.ProductGateway
import de.ma.tracker.domain.shop.ShopService
import de.ma.tracker.domain.shop.ShopGateway
import de.ma.tracker.domain.shop.ShopServiceGateway
import de.ma.tracker.domain.state.StateGateway
import io.quarkus.runtime.StartupEvent
import javax.enterprise.context.Dependent
import javax.enterprise.event.Observes
import javax.enterprise.inject.Instance
import javax.enterprise.inject.Produces
import javax.inject.Inject
import javax.transaction.Transactional

@Dependent
class BeanConfig {

    @Inject
    lateinit var shopRepository: ShopRepository

    @Inject
    lateinit var shops: Instance<ShopService>

    @Transactional
    fun onStart(@Observes event: StartupEvent) {
        shops.forEach { shop ->
            val shopEntity = ShopEntity()
            shopEntity.name = shop.name
            val existsAlready = shopRepository.existsByName(shop.name)
            if (existsAlready) {
                return@forEach
            }
            shopRepository.persist(shopEntity)
        }

    }

    @Produces
    fun productManagementUseCase(productGateway: ProductGateway): ProductManagementUseCase {
        return ProductManagementUseCaseImpl(productGateway)
    }

    @Produces
    fun stateManagementUseCase(stateGateway: StateGateway): StateManagementUseCase {
        return StateManagementUseCaseImpl(stateGateway)
    }

    @Produces
    fun addStateToProductUseCase(productGateway: ProductGateway): AddStateToProductUseCase {
        return AddStateToProductUseCaseImpl(productGateway)
    }

    @Produces
    fun addProductToShopsUseCase(productGateway: ProductGateway): AddProductToShopUseCase {
        return AddProductToShopUseCaseImpl(productGateway)
    }

    @Produces
    fun getProductsShopIdUseCase(
        productGateway: ProductGateway
    ): GetProductsByShopIdUseCase {
        return GetProductsByShopIdUseCaseImpl(productGateway)
    }

    @Produces
    fun getShopsUseCase(shopGateway: ShopGateway): GetShopsUseCase {
        return GetShopsUseCaseImpl(shopGateway)
    }

    @Produces
    fun trackPrizeOfProductUseCase(
        shopGateway: ShopGateway,
        shopServiceGateway: ShopServiceGateway,
        addStateToProductUseCase: AddStateToProductUseCase,
    ): TrackPrizeOfProductUseCase {
        return TrackPrizeOfProductUseCaseImpl(
            shopGateway,
            shopServiceGateway,
            addStateToProductUseCase
        )
    }
}