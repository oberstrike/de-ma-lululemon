package de.ma.app.data.product

import de.ma.app.data.base.toPanachePage
import de.ma.app.data.base.toPanacheSort
import de.ma.app.data.state.StateRepository
import de.ma.app.data.state.toEntity
import de.ma.tracker.domain.base.paging.Page
import de.ma.tracker.domain.base.paging.Sort
import de.ma.tracker.domain.product.ProductGateway
import de.ma.tracker.domain.product.message.ProductCreate
import de.ma.tracker.domain.product.message.ProductShow
import de.ma.tracker.domain.product.message.ProductUpdate
import de.ma.tracker.domain.state.StateCreate
import io.quarkus.hibernate.orm.panache.PanacheEntity_.id
import java.util.*
import javax.enterprise.context.ApplicationScoped
import javax.transaction.Transactional

@ApplicationScoped
class ProductGatewayImpl(
    private val productRepository: ProductRepository,
    private val stateRepository: StateRepository
) : ProductGateway {

    @Transactional
    override fun createProduct(productCreate: ProductCreate): ProductShow {
        val entity = productCreate.toEntity()
        productRepository.persist(entity)
        return entity.toShow(null)
    }

    override fun deleteProductById(userId: UUID) {
        productRepository.deleteById(userId)
    }

    override fun updateProduct(productUpdate: ProductUpdate, id: UUID): ProductShow {
        val entity: ProductEntity = productUpdate.toEntity(id)
        val states = stateRepository.findByUserId(id)
        entity.addStates(states)
        productRepository.persist(entity)
        return entity.toShow(states)
    }

    override fun getProductById(userId: UUID): ProductShow {
        val productEntity = productRepository.findById(userId)
        val states = stateRepository.findByUserId(userId)
        return productEntity.toShow(states)
    }

    override fun getList(page: Page, sort: Sort): List<ProductShow> {
        val query = productRepository.findAll(sort.toPanacheSort())
        val productEntities = query.page<ProductEntity>(page.toPanachePage()).list<ProductEntity>()
        return productEntities.mapNotNull { it.toShow(null) }
    }

    override fun pageCount(page: Page): Int {
        return productRepository.findAll().page<ProductEntity>(page.toPanachePage()).pageCount()
    }

    override fun existsById(id: UUID): Boolean {
        return productRepository.findById(id) != null
    }

    override fun addStateToProduct(productId: UUID, stateCreate: StateCreate) {
        val product = productRepository.findById(productId)
        product.addState(stateCreate.toEntity())
        productRepository.persist(product)
    }
}
