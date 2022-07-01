package de.ma.app.infrastructure.rest.product

import de.ma.app.infrastructure.rest.product.data.ProductCreateDTO
import de.ma.app.infrastructure.rest.product.data.ProductUpdateDTO
import de.ma.app.infrastructure.utils.PageRequest
import de.ma.app.infrastructure.utils.toSort
import de.ma.pricetracker.api.product.ProductManagementUseCase
import de.ma.tracker.domain.base.paging.Page
import de.ma.tracker.domain.product.message.ProductShow
import de.ma.tracker.domain.state.message.StateShow
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody
import java.util.*
import javax.ws.rs.*

@Path("/product")
class ProductRessource(
    private val productManagementUseCase: ProductManagementUseCase
) {

    @DELETE
    @Path("/{id}")
    fun deleteProductById(@PathParam("id") id: UUID) {
        productManagementUseCase.deleteProductById(id)
    }

    @Path("/{id}")
    @PUT
    fun updateProduct(@RequestBody productUpdate: ProductUpdateDTO, @PathParam("id") id: UUID): ProductShow {
        return execute {
            productManagementUseCase.updateProduct(productUpdate, id)
        }
    }

    @Path("/{id}")
    @GET
    fun getProductById(@PathParam("id") id: UUID): ProductShow {
        return execute {
            productManagementUseCase.getProductById(id)
        }
    }


    @GET
    fun getList(
        @BeanParam pageRequest: PageRequest, @QueryParam("sort") @DefaultValue("+created_at")
        sort: String = "+created_at"
    ): List<ProductShow> {
        return execute {
            productManagementUseCase.getList(Page(pageRequest.index, pageRequest.size), sort.toSort())
        }
    }

    @Path("/pageCount")
    @GET
    fun getPageCount(@BeanParam pageRequest: PageRequest): Int {
        return execute {
            productManagementUseCase.getPageCount(Page(pageRequest.index, pageRequest.size))
        }
    }


    @Path("/{id}/states")
    @GET
    fun getStates(@PathParam("id") id: UUID): List<StateShow> = execute {
        productManagementUseCase.getStates(id)
    }

    private inline fun <T> execute(block: () -> Result<T>): T {
        val result = block.invoke()
        if (result.isFailure) {
            throw result.exceptionOrNull() ?: RuntimeException("")
        }
        return result.getOrNull()!!
    }

}