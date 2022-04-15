package de.ma.lululemon.jobs

import de.ma.lululemon.jobs.pages.ScrapeTask
import de.ma.lululemon.jobs.pages.overview.overview
import kotlinx.coroutines.runBlocking
import javax.enterprise.context.ApplicationScoped


@ApplicationScoped
class ScrapeScheduldedTask {

    //@Scheduled(every = "1m", identity = "my-task")
    fun doSomething() = runBlocking {

        ScrapeTask.scrape {
            val overviewModel = overview()
            println(overviewModel.products)
        }
    }


}
