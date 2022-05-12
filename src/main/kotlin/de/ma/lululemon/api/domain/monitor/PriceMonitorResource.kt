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
    fun create(productCreateDTO: ProductCreateDTO) = priceMonitorService.createByProduct(productCreateDTO)

    @DELETE
    @Path("/{id}")
    fun deleteById(@PathParam("id") id: String) {
        priceMonitorService.deleteById(id)
    }

    @GET
    fun getAllOrders() = priceMonitorService.getAllOrders()
}