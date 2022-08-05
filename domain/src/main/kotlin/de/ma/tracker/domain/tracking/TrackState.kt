package de.ma.tracker.domain.tracking

import java.util.*

interface TrackState {
    val shopId: UUID
    val isTracked: Boolean
}