package de.ma.app.data.product

import de.ma.app.utils.AbstractDatabaseTest
import de.ma.app.utils.DatabaseTestResource
import de.ma.app.utils.TransactionalQuarkusTest
import de.ma.app.utils.sql.Sql
import io.quarkus.test.TestTransaction
import io.quarkus.test.common.QuarkusTestResource
import io.quarkus.test.junit.QuarkusTest
import org.amshove.kluent.`should be equal to`
import org.amshove.kluent.shouldBe
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.*

import javax.inject.Inject
import javax.transaction.Transactional

@QuarkusTest
class ProductGatewayImplTest : AbstractDatabaseTest() {

    @Inject
    lateinit var productGatewayImpl: ProductGatewayImpl

    @Inject
    lateinit var productRepository: ProductRepository

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
    @Sql(before = ["delete-product-test.sql"])
    @TestTransaction
    fun deleteProductById() {
        productGatewayImpl.deleteProductById(UUID.fromString("483da9d8-2401-4d9f-9504-762e3e2721d7"))

        val oProduct =
            productRepository.findByIdOptional(UUID.fromString("483da9d8-2401-4d9f-9504-762e3e2721d7"))

        oProduct.isEmpty shouldBe true
    }

    @Test
    @Sql(before = ["delete-product-test.sql"])
    @TestTransaction
    fun deleteProductByIdNegative() {
        assertThrows<RuntimeException> { productGatewayImpl.deleteProductById(UUID.fromString("483da9d8-2401-4d9f-9504-762e3e2321d7")) }

    }

    @Test
    @Sql(before = ["update-product-test.sql"])
    @TestTransaction
    fun updateProduct() {
        val id = UUID.fromString("483da9d8-2401-4d9f-9504-762e3e2721d6")

        val color = "White"
        val name = "M123"
        val size = "L"
        val pId = "PID123"

        val productShow = productGatewayImpl.updateProduct(
            ProductUpdateDTO(
                color,
                name,
                size,
                pId,
                1
            ), id
        )

        productShow.color shouldBe color
        productShow.name shouldBe name
        productShow.size shouldBe size
        productShow.pId shouldBe pId
    }

    @Test
    @Sql(before = ["delete-product-test.sql"])
    @TestTransaction
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