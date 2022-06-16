package de.ma.infrastructure.data.repository

import de.ma.infrastructure.entity.ProductEntity
import de.ma.infrastructure.entity.toEntity
import de.ma.tracker.domain.base.paging.PagedList
import de.ma.tracker.domain.base.paging.PagedRequest
import de.ma.tracker.domain.product.entity.Product
import de.ma.tracker.domain.product.repository.ProductRepository
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase
import io.quarkus.panache.common.Page
import javax.enterprise.context.ApplicationScoped
import javax.transaction.Transactional

@ApplicationScoped
class ProductRepositoryImpl : ProductRepository, PanacheRepositoryBase<ProductEntity, Long> {


    @Transactional
    override fun save(product: Product) {
        try {
            persist(product.toEntity())
        } catch (e: Exception) {
            throw RuntimeException("Could not create product", e)
        }
    }

    override fun getById(id: Long): Product? {
        return findById(id)
    }


    override fun getProductsPaged(pagedRequest: PagedRequest): PagedList<Product> {

        val page = findAll().page<ProductEntity>(Page.of(pagedRequest.page, pagedRequest.pageSize))

        return PagedList(
            pagedRequest.page,
            page.pageCount(),
            page.list()
        )

    }
}