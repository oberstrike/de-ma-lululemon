package de.ma.pricetracker.application.tracker.manager

import de.ma.tracker.domain.base.paging.PagedRequest
import de.ma.tracker.domain.base.paging.PagedList
import de.ma.pricetracker.application.tracker.converter.ProductConverter
import de.ma.pricetracker.application.tracker.converter.StateConverter
import de.ma.pricetracker.application.tracker.message.dto.ProductDTO
import de.ma.pricetracker.application.tracker.message.request.CreateStateRequest
import de.ma.pricetracker.application.tracker.message.request.UpdateProductRequest
import de.ma.pricetracker.application.tracker.message.response.*
import de.ma.tracker.domain.base.paging.mapPaged
import de.ma.tracker.domain.base.service.CrudService
import de.ma.tracker.domain.product.entity.Product
import de.ma.tracker.domain.product.service.GetProductPagedService
import de.ma.tracker.domain.product.service.GetStateByProductService

open class ProductManager(
    private val productCrudService: CrudService<Product>,
    private val getProductPagedService: GetProductPagedService,
    private val productConverter: ProductConverter,
    private val stateConverter: StateConverter,
    private val getStateByProductService: GetStateByProductService
) : IProductManager {

    override fun createProduct(createProductRequest: ProductDTO): CreateProductResponse {

        val product = productConverter.convert(createProductRequest)

        val created = productCrudService.create(product)

        return CreateProductResponse(created.id!!)
    }

    override fun getProductsPaged(pagedRequest: PagedRequest): PagedList<GetProductResponse> {
        return getProductPagedService.getProductsPaged(pagedRequest)
            .mapPaged { GetProductResponse(it) }
    }

    override fun getProductById(id: Long): GetProductResponse {

        return GetProductResponse(productCrudService.findById(id))
    }

    override fun updateProduct(id: Long, updateProductRequest: UpdateProductRequest) {
        val product = productConverter.convert(updateProductRequest, id)
        productCrudService.update(product)
    }

    override fun deleteProductById(id: Long) {

        productCrudService.deleteById(id)
    }

    override fun addStateToProduct(productId: Long, createStateRequest: CreateStateRequest): CreateStateResponse {
        val state = stateConverter.convert(createStateRequest)
        val product = productCrudService.findById(productId)

        product.addState(state)

        productCrudService.update(product)

        return CreateStateResponse(state)
    }

    override fun removeStatesFromProduct(productId: Long) {
        productCrudService.findById(productId).removeStates()
    }

    override fun getStatesOfProduct(productId: Long): List<GetStateResponse> {
        return getStateByProductService.getStateByProduct(productId).map {
            GetStateResponse(it)
        }
    }

}