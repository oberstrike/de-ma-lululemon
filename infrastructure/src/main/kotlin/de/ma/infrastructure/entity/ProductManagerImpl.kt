package de.ma.infrastructure.entity

import de.ma.pricetracker.application.tracker.converter.ProductConverter
import de.ma.pricetracker.application.tracker.converter.StateConverter
import de.ma.pricetracker.application.tracker.manager.ProductManager
import de.ma.tracker.domain.base.service.CrudService
import de.ma.tracker.domain.product.entity.Product
import de.ma.tracker.domain.product.service.GetProductPagedService
import de.ma.tracker.domain.product.service.GetStateByProductService
import javax.enterprise.context.ApplicationScoped

@ApplicationScoped
class ProductManagerImpl(
    productCrudService: CrudService<Product>,
    getProductPagedService: GetProductPagedService,
    productConverter: ProductConverter,
    stateConverter: StateConverter,
    getStateByProductService: GetStateByProductService
) : ProductManager(
    productCrudService, getProductPagedService, productConverter, stateConverter,
    getStateByProductService
) {
}