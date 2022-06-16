package de.ma.pricetracker.application.tracker.converter

import de.ma.pricetracker.application.tracker.message.dto.ProductDTO
import de.ma.pricetracker.application.tracker.message.request.UpdateProductRequest
import de.ma.tracker.domain.product.entity.Product

interface ProductConverter {
    fun convert(request: ProductDTO): Product
    fun convert(request: UpdateProductRequest, id: Long): Product
}