package de.ma.tracker.domain.product

import de.ma.tracker.domain.base.IBaseEntity
import de.ma.tracker.domain.state.State
import de.ma.tracker.domain.tracking.TrackState
import java.time.LocalDateTime


interface Product : IBaseEntity {

    var pId: String?

    var color: String

    var size: String

    var name: String

    var version: Long

    val states: Set<State>

    val createdAt: LocalDateTime

    var updatedAt: LocalDateTime?

    val trackState: TrackState

}