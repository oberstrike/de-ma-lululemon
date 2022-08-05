package de.ma.tracker.domain.state.message

import java.time.LocalDateTime
import java.util.*

interface StateShow {
    val price: Float
    val entryDate: LocalDateTime
}