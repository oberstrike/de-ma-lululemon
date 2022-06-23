package de.ma.app.infrastructure.config

import de.ma.pricetracker.api.product.ProductManagementUseCase
import de.ma.pricetracker.impl.product.ProductManagementUseCaseImpl
import de.ma.tracker.domain.product.ProductGateway
import javax.enterprise.context.Dependent
import javax.enterprise.inject.Produces

@Dependent
class BeanConfig {

    @Produces
    fun productManagementUseCase(productGateway: ProductGateway): ProductManagementUseCase {
        return ProductManagementUseCaseImpl(productGateway)
    }
}