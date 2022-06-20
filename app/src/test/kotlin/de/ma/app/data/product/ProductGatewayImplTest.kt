package de.ma.app.data.product

import de.ma.tracker.domain.product.message.ProductCreate
import io.quarkus.test.junit.QuarkusTest
import org.amshove.kluent.`should be equal to`
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import javax.inject.Inject

@QuarkusTest
class ProductGatewayImplTest {

    @Inject
    lateinit var productGatewayImpl: ProductGatewayImpl

    @Test
    fun createProduct() {
        val productShow = productGatewayImpl.createProduct(
            ProductCreateDTO(
                color = "Black",
                pId = "Test123",
                name = "Test123",
                size = "L"
            )
        )

        "Black" `should be equal to` productShow.color
        "Test123" `should be equal to` productShow.pId
        "Test123" `should be equal to` productShow.name
        "L" `should be equal to` productShow.size
    }

    @Test
    fun deleteProductById() {
    }

    @Test
    fun updateProduct() {
    }

    @Test
    fun getProductById() {
    }

    @Test
    fun getList() {
    }

    @Test
    fun pageCount() {
    }

    @Test
    fun existsById() {
    }

    @Test
    fun addStateToProduct() {
    }
}