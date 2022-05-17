package de.ma.lululemon.api.domain.monitor

import javax.ws.rs.*
import javax.ws.rs.core.MediaType

@Path("/monitor")
class PriceMonitorResource(
    val priceMonitorService: PriceMonitorService
) {

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    fun create(priceMonitorCreate: PriceMonitorCreate) = priceMonitorService.create(priceMonitorCreate).toDTO()

    @DELETE
    @Path("/{id}")
    fun deleteById(@PathParam("id") id: String) {
        priceMonitorService.deleteById(id)
    }

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    fun getById(@PathParam("id") id: String) = priceMonitorService.getById(id).toDTO()

    @GET
    fun getAllOrders() = priceMonitorService.getAllOrders()
}