package de.ma.lululemon.api.domain.monitor.product

import java.time.LocalDateTime

class State {
    var price: Float = 0.0f

    var timestamp: LocalDateTime = LocalDateTime.now()

    var available: Boolean = false
}

