package de.ma.app.data.product

import de.ma.app.data.base.`is not content of`
import de.ma.app.data.base.toPanachePage
import de.ma.app.data.base.toPanacheSort
import de.ma.app.data.shop.ShopEntity
import de.ma.app.data.state.StateRepository
import de.ma.app.data.state.toEntity
import de.ma.app.data.state.toShow
import de.ma.tracker.domain.base.paging.Page
import de.ma.tracker.domain.base.paging.Sort
import de.ma.tracker.domain.product.Product
import de.ma.tracker.domain.product.ProductGateway
import de.ma.tracker.domain.product.message.ProductCreate
import de.ma.tracker.domain.product.message.ProductShow
import de.ma.tracker.domain.product.message.ProductUpdate
import de.ma.tracker.domain.state.message.StateCreate
import de.ma.tracker.domain.state.message.StateShow
import java.util.*
import javax.enterprise.context.ApplicationScoped
import javax.transaction.Transactional

@ApplicationScoped
class ProductGatewayImpl(
    private val productRepository: ProductRepository,
    private val stateRepository: StateRepository
) : ProductGateway {

    companion object {
        private val ALLOWED_COLUMNS = listOf("id", "created_at")
    }


    @Transactional
    override fun createProduct(productCreate: ProductCreate, shopId: UUID): ProductShow {
        val entity = productCreate.toEntity()

        entity.shop = ShopEntity().apply {
            id = shopId
        }

        productRepository.persist(entity)
        return entity.toShow(null)
    }

    override fun getProductsByShopId(shopId: UUID): List<Product> {
        return productRepository.findByShopId(shopId)
    }


    override fun deleteProductById(userId: UUID) {
        val deleteById = productRepository.deleteById(userId)
        if (!deleteById) {
            throw RuntimeException("Product ($userId) couldn't be deleted.")
        }
    }

    @Transactional
    override fun updateProduct(productUpdate: ProductUpdate, id: UUID): ProductShow {
        if (!existsById(id)) {
            throw RuntimeException("Product with id $id does not exist.")
        }

        var entity = productUpdate.toEntity(id)

        val states = stateRepository.findByProductId(id)

        entity.addStates(states)
        val entityManager = productRepository.entityManager
        entity = entityManager.merge(entity)
        return entity.toShow(states)
    }

    override fun getProductById(userId: UUID): ProductShow {
        val productEntity = productRepository.findById(userId)
        val states = stateRepository.findByProductId(userId)
        return productEntity.toShow(states)
    }

    override fun getList(page: Page, sort: Sort): List<ProductShow> {
        if (sort.column.`is not content of`(ALLOWED_COLUMNS)) {
            throw IllegalArgumentException("The column ${sort.column} is not valid.")
        }

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

    override fun addStateToProduct(productId: UUID, stateCreate: StateCreate): StateShow {
        val product = productRepository.findById(productId)
        val state = stateCreate.toEntity()
        product.addState(state)
        productRepository.persist(product)
        return state.toShow()
    }

    override fun getStates(id: UUID): List<StateShow> {
        return stateRepository.findByProductId(id).map { it.toShow() }
    }

}
