package de.ma.lululemon.jobs.pages

import it.skrape.fetcher.Result

data class Page<T>(
    val url: String,
    val task: Result.() -> T
)
