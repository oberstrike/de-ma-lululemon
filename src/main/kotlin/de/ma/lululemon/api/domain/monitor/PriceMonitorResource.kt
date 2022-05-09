package de.ma.lululemon.api.domain.monitor

import de.ma.lululemon.api.domain.product.ProductCreateDTO
import javax.ws.rs.*
import javax.ws.rs.core.MediaType

@Path("/monitor")
class PriceMonitorResource(
    val priceMonitorService: PriceMonitorService
) {

    @Path("/url")
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    fun createWithUrl(@FormParam("url") url: String) = priceMonitorService.createByUrl(url)

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    fun create(productCreateDTO: ProductCreateDTO) = priceMonitorService.createByOrder(productCreateDTO)


    @GET
    fun getAllOrders() = priceMonitorService.getAllOrders()
}