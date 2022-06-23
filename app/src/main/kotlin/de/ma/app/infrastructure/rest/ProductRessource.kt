package de.ma.app.infrastructure.rest

import de.ma.pricetracker.api.product.ProductManagementUseCase
import de.ma.tracker.domain.base.paging.Page
import de.ma.tracker.domain.base.paging.Sort
import de.ma.tracker.domain.product.message.ProductCreate
import de.ma.tracker.domain.product.message.ProductShow
import de.ma.tracker.domain.product.message.ProductUpdate
import java.util.*
import javax.ws.rs.DELETE
import javax.ws.rs.GET
import javax.ws.rs.POST
import javax.ws.rs.PUT
import javax.ws.rs.Path

@Path("/product")
class ProductRessource(
    private val productManagementUseCase: ProductManagementUseCase
) {

    @POST
    fun createProduct(productCreate: ProductCreate): ProductShow {
        return execute {
            productManagementUseCase.createProduct(productCreate)
        }
    }

    @DELETE
    fun deleteProductById(id: UUID) {
        productManagementUseCase.deleteProductById(id)
    }

    @PUT
    fun updateProduct(productUpdate: ProductUpdate, productId: UUID): ProductShow {
        return execute {
            productManagementUseCase.updateProduct(productUpdate, productId)
        }
    }

    @Path("/{id}")
    @GET
    fun getProductById(id: UUID): ProductShow {
        return execute {
            productManagementUseCase.getProductById(id)
        }
    }


    @GET
    fun getList(page: Page, sort: Sort): List<ProductShow> {
        return execute {
            productManagementUseCase.getList(page, sort)
        }
    }

    @Path("/pageCount")
    @GET
    fun getPageCount(page: Page): Int {
        return execute {
            productManagementUseCase.getPageCount(page)
        }
    }

    private inline fun <T> execute(block: () -> Result<T>): T {
        val result = block.invoke()
        if (result.isFailure) {
            throw result.exceptionOrNull() ?: RuntimeException("")
        }
        return result.getOrNull()!!
    }

}