package de.ma.app.data.state

import de.ma.tracker.domain.state.message.StateShow

fun StateEntity.toShow(): StateShow {
    return StateShowDTO(
        this.price,
        this.id!!,
        this.entryDate
    )
}