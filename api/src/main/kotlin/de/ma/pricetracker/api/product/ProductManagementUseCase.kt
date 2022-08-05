package de.ma.pricetracker.api.product

import de.ma.tracker.domain.base.paging.Page
import de.ma.tracker.domain.base.paging.Sort
import de.ma.tracker.domain.product.message.ProductCreate
import de.ma.tracker.domain.product.message.ProductOverview
import de.ma.tracker.domain.product.message.ProductShow
import de.ma.tracker.domain.product.message.ProductUpdate
import de.ma.tracker.domain.state.message.StateShow
import java.util.*

interface ProductManagementUseCase {

    fun deleteProductById(id: UUID)

    fun updateProduct(productUpdate: ProductUpdate, productId: UUID): Result<ProductShow>

    fun getProductById(id: UUID): Result<ProductShow>

    fun getList(page: Page, sort: Sort): Result<List<ProductOverview>>

    fun getPageCount(page: Page): Result<Int>

    fun getStates(id: UUID): Result<List<StateShow>>

}