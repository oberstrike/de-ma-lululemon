package de.ma.app.infrastructure.utils

import javax.ws.rs.DefaultValue
import javax.ws.rs.QueryParam

class PageRequest {
    @QueryParam("pageNum")
    @DefaultValue("0")
    val index = 0

    @QueryParam("pageSize")
    @DefaultValue("10")
    val size = 0
}
