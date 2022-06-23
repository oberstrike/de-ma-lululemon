package de.ma.pricetracker.impl.product

import de.ma.pricetracker.api.product.ProductManagementUseCase
import de.ma.tracker.domain.base.paging.Page
import de.ma.tracker.domain.base.paging.Sort
import de.ma.tracker.domain.product.ProductGateway
import de.ma.tracker.domain.product.message.ProductCreate
import de.ma.tracker.domain.product.message.ProductShow
import de.ma.tracker.domain.product.message.ProductUpdate
import java.util.*


class ProductManagementUseCaseImpl(
    private val productGateway: ProductGateway
) : ProductManagementUseCase {
    override fun createProduct(productCreate: ProductCreate): Result<ProductShow> {
        return run {
            productGateway.createProduct(productCreate)
        }
    }

    override fun deleteProductById(id: UUID) {
        val exists = productGateway.existsById(id)
        if (!exists) {
            throw NoSuchElementException("Product with id $id does not exist")
        }
        productGateway.deleteProductById(id)
    }

    override fun updateProduct(productUpdate: ProductUpdate, productId: UUID): Result<ProductShow> {
        return run {
            productGateway.updateProduct(productUpdate, productId)
        }
    }

    override fun getProductById(id: UUID): Result<ProductShow> {
        return run {
            productGateway.getProductById(id)
        }
    }

    override fun getList(page: Page, sort: Sort): Result<List<ProductShow>> {
        return run {
            productGateway.getList(page, sort)
        }
    }

    override fun getPageCount(page: Page): Result<Int> {
        return run {
            productGateway.pageCount(page)
        }
    }


    private inline fun <reified T> run(block: () -> T): Result<T> {
        return try {
            Result.success(block())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

}