package de.ma.tracker.domain.product.service

import de.ma.tracker.domain.base.service.CrudService
import de.ma.tracker.domain.product.entity.Product
import de.ma.tracker.domain.product.entity.State
import de.ma.tracker.domain.product.repository.ProductRepository

class GetStateByProductService(
    private val productCrudService: CrudService<Product>
) {

    fun getStateByProduct(id: Long): Set<State> {
        return productCrudService.findById(id).states
    }

}