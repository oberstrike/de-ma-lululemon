package de.ma.tracker.domain.base.paging


data class Sort(
    val column: String,
    val direction: Direction
)

enum class Direction{
    ASC,
    DESC
}