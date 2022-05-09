package de.ma.lululemon.api.domain.product

import javax.ws.rs.GET
import javax.ws.rs.Path

@Path("/products")
class ProductResource(
    private val productService: ProductService
) {

    @GET
    @Path("/{id:\\s+}")
    fun getById(id: String): ProductShowDTO {
        return productService.byId(id).toShowDTO()
    }

    @GET
    fun products(): List<ProductShowDTO> {
        return productService.all()
    }

}