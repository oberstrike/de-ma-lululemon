package de.ma.tracker.domain.state.message

import java.time.LocalDateTime

interface StateCreate {
    val price: Float
    val entryDate: LocalDateTime?
}