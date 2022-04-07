package de.ma.lululemon.jobs

import io.quarkus.scheduler.Scheduled
import it.skrape.core.htmlDocument
import it.skrape.fetcher.BrowserFetcher
import it.skrape.fetcher.response
import it.skrape.fetcher.skrape
import it.skrape.selects.html5.link
import kotlinx.coroutines.runBlocking
import javax.enterprise.context.ApplicationScoped

@ApplicationScoped
class ScrapeTask {

    @Scheduled(every = "1m", identity = "my-task")
    fun doSomething() = runBlocking {

        val client = skrape(BrowserFetcher) {
            request {
                timeout = 10_000
                followRedirects = true
                url = "https://www.lululemon.de/en-de/c/mens/shorts?sz=100"
            }
        }

        client.response {
            htmlDocument {
                this.link {
                    this.
                }
            }
        }
    }

}
