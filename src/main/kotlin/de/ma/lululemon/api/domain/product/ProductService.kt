package de.ma.lululemon.api.domain.product

import org.bson.types.ObjectId
import javax.enterprise.context.ApplicationScoped


@ApplicationScoped
class ProductService(
    private val productRepository: ProductRepository
) {

    fun byId(id: String): ProductEntity {
        val productEntity = productRepository.findById(ObjectId(id))
            ?: throw IllegalArgumentException("Product with id $id not found")
        return productEntity
    }

    fun all(): List<ProductShowDTO> {
        return productRepository.findAll().list().map { it.toShowDTO() }
    }


    fun create(productCreateDTO: ProductCreateDTO): ProductEntity {
        val product = productCreateDTO.toEntity()
        try {
            productRepository.persist(product)
            return product
        } catch (e: Exception) {
            throw IllegalArgumentException("The product $productCreateDTO couldn't be created")
        }
    }

}