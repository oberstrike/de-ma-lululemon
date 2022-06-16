package de.ma.pricetracker.application.tracker.controller

import de.ma.pricetracker.application.tracker.manager.IProductManager
import de.ma.pricetracker.application.tracker.message.dto.ProductDTO
import de.ma.tracker.domain.base.paging.PagedRequest
import de.ma.tracker.domain.base.paging.PagedList
import de.ma.pricetracker.application.tracker.message.request.CreateStateRequest
import de.ma.pricetracker.application.tracker.message.request.UpdateProductRequest
import de.ma.pricetracker.application.tracker.message.response.*

interface ProductController : IProductManager{

    //POST /api/products
    override fun createProduct(createProductRequest: ProductDTO): CreateProductResponse

    //GET /api/products
    override fun getProductsPaged(pagedRequest: PagedRequest): PagedList<GetProductResponse>

    //GET /api/products/{id}
    override fun getProductById(id: Long): GetProductResponse

    //PUT /api/products/{id}
    override fun updateProduct(id: Long, updateProductRequest: UpdateProductRequest)

    //DELETE /api/products/{id}
    override fun deleteProductById(id: Long)

    //POST /api/products/{id}/states
    override fun addStateToProduct(productId: Long, createStateRequest: CreateStateRequest): CreateStateResponse

    //DELETE /api/products/{id}/states
    override fun removeStatesFromProduct(productId: Long)

    //GET /api/products/{id}/states
    override fun getStatesOfProduct(productId: Long): List<GetStateResponse>


}