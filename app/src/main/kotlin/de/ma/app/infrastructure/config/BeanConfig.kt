package de.ma.app.infrastructure.config

import de.ma.pricetracker.api.product.AddStateToProductUseCase
import de.ma.pricetracker.api.product.ProductManagementUseCase
import de.ma.pricetracker.api.product.TrackPrizeUseCase
import de.ma.pricetracker.api.state.StateManagementUseCase
import de.ma.pricetracker.impl.product.AddStateToProductUseCaseImpl
import de.ma.pricetracker.impl.product.ProductManagementUseCaseImpl
import de.ma.pricetracker.impl.shop.TrackPrizeUseCaseImpl
import de.ma.pricetracker.impl.state.StateManagementUseCaseImpl
import de.ma.tracker.domain.product.ProductGateway
import de.ma.tracker.domain.shop.ShopService
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
    fun trackPrizeOfProductUseCase(
        shopServiceGateway: ShopServiceGateway,
        addStateToProductUseCase: AddStateToProductUseCase,
        productGateway: ProductGateway
    ): TrackPrizeUseCase {
        return TrackPrizeUseCaseImpl(
            shopServiceGateway,
            addStateToProductUseCase,
            productGateway
        )
    }
}