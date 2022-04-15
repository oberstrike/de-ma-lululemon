package de.ma.lululemon.jobs.pages.overview

data class ProductOverview(
    val pid: String,
    val name: String,
    val prices: List<Float>
)