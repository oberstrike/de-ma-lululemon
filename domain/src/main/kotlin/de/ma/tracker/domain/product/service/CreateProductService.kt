package de.ma.tracker.domain.product.service

import de.ma.tracker.domain.product.entity.Product
import de.ma.tracker.domain.product.repository.ProductRepository



class CreateProductService(
    private val productRepository: ProductRepository
) {

    fun create(product: Product): Product {
        return productRepository.create(product)
    }
}