package de.ma.tracker.domain.product

import de.ma.tracker.domain.base.paging.Page
import de.ma.tracker.domain.base.paging.Sort
import de.ma.tracker.domain.product.message.ProductCreate
import de.ma.tracker.domain.product.message.ProductOverview
import de.ma.tracker.domain.product.message.ProductShow
import de.ma.tracker.domain.product.message.ProductUpdate
import de.ma.tracker.domain.state.message.StateCreate
import de.ma.tracker.domain.state.message.StateShow
import java.util.*

interface ProductGateway {

    fun createProduct(productCreate: ProductCreate, shopId: UUID): ProductOverview

    fun deleteProductById(userId: UUID)

    fun updateProduct(productUpdate: ProductUpdate, id: UUID): ProductShow

    fun getProductById(userId: UUID): ProductShow

    fun getList(page: Page, sort: Sort): List<ProductOverview>

    fun pageCount(page: Page): Int

    fun existsById(id: UUID): Boolean

    fun addStateToProduct(productId: UUID, stateCreate: StateCreate): StateShow

    fun getStates(productId: UUID): List<StateShow>

    fun getTrackedProducts(): List<ProductOverview>

}