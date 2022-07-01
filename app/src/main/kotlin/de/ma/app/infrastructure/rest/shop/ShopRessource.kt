package de.ma.app.infrastructure.rest.shop

import de.ma.pricetracker.api.shop.AddProductToShopUseCase
import de.ma.pricetracker.api.shop.GetProductsByShopIdUseCase
import de.ma.pricetracker.api.shop.GetShopsUseCase
import de.ma.tracker.domain.product.Product
import de.ma.tracker.domain.product.message.ProductCreate
import de.ma.tracker.domain.product.message.ProductShow
import de.ma.tracker.domain.shop.Shop
import java.util.*
import javax.ws.rs.GET
import javax.ws.rs.POST
import javax.ws.rs.Path

@Path("/shop")
class ShopRessource(
    private val getProductsByShopIdUseCase: GetProductsByShopIdUseCase,
    private val getShopsUseCase: GetShopsUseCase,
    private val addProductToShopsUseCase: AddProductToShopUseCase
) {
    @Path("/{id}/product")
    @GET
    fun getProductsByShopId(id: UUID): List<Product> {
        return getProductsByShopIdUseCase.execute(id)
    }

    @Path("/{id}/products")
    @POST
    fun addProductToShop(productCreate: ProductCreate, id: UUID): ProductShow {
        return addProductToShopsUseCase.execute(productCreate, id)
    }

    @GET
    fun getShops(): List<Shop> {
        return getShopsUseCase.execute()
    }


}
