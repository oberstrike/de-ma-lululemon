package de.ma.pricetracker.impl.product

import de.ma.pricetracker.api.product.AddStateToProductUseCase
import de.ma.tracker.domain.product.ProductGateway
import de.ma.tracker.domain.state.message.StateCreate
import java.util.*

class AddStateToProductUseCaseImpl(
    private val productGateway: ProductGateway
) : AddStateToProductUseCase {


    override fun execute(productId: UUID, stateCreate: StateCreate) {
        val existsById = productGateway.existsById(productId)
        if(!existsById) {
            throw IllegalArgumentException("Product with id $productId does not exist")
        }
        productGateway.addStateToProduct(productId, stateCreate)

    }
}