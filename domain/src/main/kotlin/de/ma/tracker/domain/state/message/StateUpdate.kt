package de.ma.tracker.domain.state.message

import java.time.LocalDateTime

interface StateUpdate {
    val price: Long
    val entryDate: LocalDateTime
}