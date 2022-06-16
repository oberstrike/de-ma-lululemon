package de.ma.infrastructure.rest

import de.ma.pricetracker.application.tracker.controller.ProductController
import de.ma.pricetracker.application.tracker.manager.ProductManager
import de.ma.pricetracker.application.tracker.message.dto.ProductDTO
import de.ma.pricetracker.application.tracker.message.request.CreateStateRequest
import de.ma.pricetracker.application.tracker.message.request.UpdateProductRequest
import de.ma.pricetracker.application.tracker.message.response.CreateProductResponse
import de.ma.pricetracker.application.tracker.message.response.CreateStateResponse
import de.ma.pricetracker.application.tracker.message.response.GetProductResponse
import de.ma.pricetracker.application.tracker.message.response.GetStateResponse
import de.ma.tracker.domain.base.paging.PagedList
import de.ma.tracker.domain.base.paging.PagedRequest
import javax.ws.rs.Consumes
import javax.ws.rs.Path
import javax.ws.rs.Produces
import javax.ws.rs.core.MediaType

@Path("/product")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
class ProductRestController(
    private val productManager: ProductManager
) : ProductController {


    override fun createProduct(createProductRequest: ProductDTO): CreateProductResponse {
        return productManager.createProduct(createProductRequest)
    }

    override fun getProductsPaged(pagedRequest: PagedRequest): PagedList<GetProductResponse> {
        return productManager.getProductsPaged(pagedRequest)
    }

    override fun getProductById(id: Long): GetProductResponse {
        return productManager.getProductById(id)
    }

    override fun updateProduct(id: Long, updateProductRequest: UpdateProductRequest) {
        productManager.updateProduct(id, updateProductRequest)
    }

    override fun deleteProductById(id: Long) {
        productManager.deleteProductById(id)
    }

    override fun addStateToProduct(productId: Long, createStateRequest: CreateStateRequest): CreateStateResponse {
        return productManager.addStateToProduct(productId, createStateRequest)
    }

    override fun removeStatesFromProduct(productId: Long) {
        productManager.removeStatesFromProduct(productId)
    }

    override fun getStatesOfProduct(productId: Long): List<GetStateResponse> {
        return productManager.getStatesOfProduct(productId)
    }
}