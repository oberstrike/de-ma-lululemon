package de.ma.lululemon.api.domain.entry

import java.time.LocalDateTime

class Entry{
    var price: Float = 0.0f

    var timestamp: LocalDateTime = LocalDateTime.now()

    var available: Boolean = false
}

