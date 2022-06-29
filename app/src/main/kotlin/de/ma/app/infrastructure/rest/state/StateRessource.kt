package de.ma.app.infrastructure.rest.state

import de.ma.app.infrastructure.rest.state.data.StateUpdateDTO
import de.ma.pricetracker.api.state.StateManagementUseCase
import de.ma.tracker.domain.state.message.StateShow
import de.ma.tracker.domain.state.message.StateUpdate
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody
import java.util.*
import javax.ws.rs.DELETE
import javax.ws.rs.GET
import javax.ws.rs.PUT
import javax.ws.rs.Path
import javax.ws.rs.PathParam

@Path("/state")
class StateRessource(
    private val stateManagementUseCase: StateManagementUseCase
) {

    @Path("/{id}")
    @PUT
    fun updateState(@PathParam("id") id: UUID, @RequestBody stateUpdate: StateUpdateDTO): StateShow {
       return stateManagementUseCase.updateStateById(stateUpdate, id)
    }

    @Path("/{id}")
    @DELETE
    fun deleteStateById(@PathParam("id") id: UUID){
        stateManagementUseCase.deleteStateById(id)
    }


}