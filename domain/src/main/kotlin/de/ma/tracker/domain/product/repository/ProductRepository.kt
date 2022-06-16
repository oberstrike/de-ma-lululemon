package de.ma.tracker.domain.product.repository

import de.ma.tracker.domain.base.paging.PagedList
import de.ma.tracker.domain.base.paging.PagedRequest
import de.ma.tracker.domain.product.entity.Product

interface ProductRepository {

    fun getById(id: Long): Product?

    fun save(product: Product)

    fun getProductsPaged(pagedRequest: PagedRequest): PagedList<Product>
}