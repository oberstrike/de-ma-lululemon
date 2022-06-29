package de.ma.tracker.domain.base.paging

data class Page(
    val index: Int = 0,
    val size: Int = 10
) {

    init {
        require(index >= 0) { "Page index must be >= 0" }
        require(size >= 0) { "Page size must be >= 0" }
    }
}