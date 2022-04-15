package de.ma.lululemon.jobs.pages

import it.skrape.fetcher.HttpFetcher
import it.skrape.fetcher.response
import it.skrape.fetcher.skrape

object ScrapeTask {

    private val client = skrape(HttpFetcher) {
        request {
            timeout = 10_000
            followRedirects = true
            url = "https://www.lululemon.de/en-de/c/mens/shorts?sz=100"
        }
    }

    suspend fun <T> fetch(block: () -> Page<T>): T {
        val page = block.invoke()
        return client.apply {
            request {
                url = page.url
            }
        }.response {
            page.task.invoke(this)
        }
    }

    suspend fun scrape(block: suspend ScrapeTask.() -> Unit) {
        block.invoke(this)
    }

}