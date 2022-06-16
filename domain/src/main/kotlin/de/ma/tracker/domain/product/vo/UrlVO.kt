package de.ma.tracker.domain.product.vo

import de.ma.tracker.domain.base.isUrl

data class UrlVO(
    val url: String
){

    init {
        require(url.isNotBlank()) { "Name is blank" }
        require(url.isUrl())
    }


}