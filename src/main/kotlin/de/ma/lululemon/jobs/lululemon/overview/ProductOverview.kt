package de.ma.lululemon.jobs.lululemon.overview

import java.time.LocalDateTime

data class ProductOverview(
    val pid: String,
    val name: String,
    val prices: List<Float>,
    val dateTime: LocalDateTime,
    val link: String
)
