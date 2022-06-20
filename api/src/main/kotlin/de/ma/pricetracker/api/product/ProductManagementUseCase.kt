package de.ma.pricetracker.api.product

import de.ma.tracker.domain.base.paging.Page
import de.ma.tracker.domain.base.paging.Sort
import de.ma.tracker.domain.product.message.ProductCreate
import de.ma.tracker.domain.product.message.ProductShow
import de.ma.tracker.domain.product.message.ProductUpdate
import java.util.*

interface ProductManagementUseCase {

    fun createProduct(productCreate: ProductCreate): Result<ProductShow>

    fun deleteProductById(id: UUID)

    fun updateProduct(productUpdate: ProductUpdate, productId: UUID): Result<ProductShow>

    fun getProductById(id: UUID): Result<ProductShow>

    fun getList(page: Page, sort: Sort): Result<List<ProductShow>>

    fun getPageCount(page: Page): Result<Int>

}