package de.ma.lululemon.api.domain.monitor

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
    fun create(product: Product) = priceMonitorService.createByProduct(product)


    @GET
    fun getAllOrders() = priceMonitorService.getAllOrders()
}