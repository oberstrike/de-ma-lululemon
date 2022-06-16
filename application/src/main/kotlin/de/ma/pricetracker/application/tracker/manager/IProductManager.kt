package de.ma.pricetracker.application.tracker.manager

import de.ma.pricetracker.application.tracker.message.dto.ProductDTO
import de.ma.pricetracker.application.tracker.message.request.CreateStateRequest
import de.ma.pricetracker.application.tracker.message.request.UpdateProductRequest
import de.ma.pricetracker.application.tracker.message.response.CreateProductResponse
import de.ma.pricetracker.application.tracker.message.response.CreateStateResponse
import de.ma.pricetracker.application.tracker.message.response.GetProductResponse
import de.ma.pricetracker.application.tracker.message.response.GetStateResponse
import de.ma.tracker.domain.base.paging.PagedList
import de.ma.tracker.domain.base.paging.PagedRequest

interface IProductManager {
    fun createProduct(createProductRequest: ProductDTO): CreateProductResponse
    fun getProductsPaged(pagedRequest: PagedRequest): PagedList<GetProductResponse>
    fun getProductById(id: Long): GetProductResponse
    fun updateProduct(id: Long, updateProductRequest: UpdateProductRequest)
    fun deleteProductById(id: Long)
    fun addStateToProduct(productId: Long, createStateRequest: CreateStateRequest): CreateStateResponse
    fun removeStatesFromProduct(productId: Long)
    fun getStatesOfProduct(productId: Long): List<GetStateResponse>
}