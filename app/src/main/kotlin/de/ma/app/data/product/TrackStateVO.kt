package de.ma.app.data.product

import de.ma.tracker.domain.tracking.TrackState
import java.util.*

data class TrackStateVO(
    override val isTracked: Boolean = false,
    override val shopId: UUID
): TrackState