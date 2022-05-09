package de.ma.lululemon.api.common

import javax.ws.rs.core.Response
import javax.ws.rs.ext.ExceptionMapper
import javax.ws.rs.ext.Provider


@Provider
class IllegalArgumentExceptionMapper : ExceptionMapper<IllegalArgumentException> {

    override fun toResponse(exception: IllegalArgumentException?): Response {
        return Response.status(Response.Status.BAD_REQUEST).entity(exception.toString()).build()
    }
}