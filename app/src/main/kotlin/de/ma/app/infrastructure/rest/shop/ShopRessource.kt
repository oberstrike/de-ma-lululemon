package de.ma.app.infrastructure.rest.shop

import de.ma.app.infrastructure.rest.product.data.ProductCreateDTO
import de.ma.pricetracker.api.product.ProductManagementUseCase
import de.ma.tracker.domain.product.message.ProductOverview
import de.ma.tracker.domain.shop.Shop
import de.ma.tracker.domain.shop.ShopServiceGateway
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody
import java.util.*
import javax.ws.rs.GET
import javax.ws.rs.POST
import javax.ws.rs.Path
import javax.ws.rs.PathParam

@Path("/shop")
class ShopRessource(
    private val shopServiceGateway: ShopServiceGateway
) {

    @GET
    fun getShops(): List<Shop> {
        return shopServiceGateway.getAllShops().map { it.shop }
    }


}
