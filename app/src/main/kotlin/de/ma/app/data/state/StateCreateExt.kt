package de.ma.app.data.state

import de.ma.tracker.domain.state.message.StateCreate

fun StateCreate.toEntity(): StateEntity {
    val stateEntity = StateEntity()
    stateEntity.price = price
    return stateEntity
}

