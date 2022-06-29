package de.ma.app.infrastructure.utils

import de.ma.tracker.domain.base.paging.Direction
import de.ma.tracker.domain.base.paging.Sort

fun String.toSort(): Sort {
    if (isEmpty()) {
        throw IllegalArgumentException("Sorting string is empty")
    }

    val direction = when (first()) {
        '-' -> Direction.DESC
        else -> Direction.ASC
    }

    return Sort(this.replace(Regex("[+-]"), ""), direction)
}