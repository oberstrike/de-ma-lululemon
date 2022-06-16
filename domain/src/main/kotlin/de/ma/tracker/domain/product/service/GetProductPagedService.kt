package de.ma.tracker.domain.product.service

import de.ma.tracker.domain.base.paging.PagedList
import de.ma.tracker.domain.base.paging.PagedRequest
import de.ma.tracker.domain.product.entity.Product
import de.ma.tracker.domain.product.repository.ProductRepository

class GetProductPagedService(
    private val productRepository: ProductRepository
) {

    fun getProductsPaged(pagedRequest: PagedRequest): PagedList<Product> {
        return productRepository.getProductsPaged(pagedRequest)
    }
}