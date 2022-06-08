package de.ma.tracker.domain.product.vo

import de.ma.tracker.domain.base.isUrl
import javax.persistence.Access
import javax.persistence.AccessType
import javax.persistence.Embeddable

@Embeddable
data class UrlVO(
    @get:Access(AccessType.FIELD)
    val url: String
){

    init {
        require(url.isNotBlank()) { "Name is blank" }
        require(url.isUrl())
    }


}