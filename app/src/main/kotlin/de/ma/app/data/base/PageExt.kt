package de.ma.app.data.base

import de.ma.tracker.domain.base.paging.Direction
import de.ma.tracker.domain.base.paging.Page
import de.ma.tracker.domain.base.paging.Sort

fun Page.toPanachePage(): io.quarkus.panache.common.Page {
    return io.quarkus.panache.common.Page.of(this.index, this.size)
}

fun io.quarkus.panache.common.Page.toDomainPage(): Page {
    return Page(index, size)
}

fun Sort.toPanacheSort(): io.quarkus.panache.common.Sort {
    return io.quarkus.panache.common.Sort.by(this.column, this.direction.toPanacheDirection())
}

fun io.quarkus.panache.common.Sort.toDomainSort(): Sort {
    return Sort(columns.first().name, columns.first().direction.toDomainDirection())
}


fun Direction.toPanacheDirection(): io.quarkus.panache.common.Sort.Direction {
    return when (this) {
        Direction.ASC -> io.quarkus.panache.common.Sort.Direction.Ascending
        Direction.DESC -> io.quarkus.panache.common.Sort.Direction.Descending
    }
}

fun io.quarkus.panache.common.Sort.Direction.toDomainDirection(): Direction {
    return when (this) {
        io.quarkus.panache.common.Sort.Direction.Ascending -> Direction.ASC
        io.quarkus.panache.common.Sort.Direction.Descending -> Direction.DESC
    }
}