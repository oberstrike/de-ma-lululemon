package de.ma.lululemon.jobs

import de.ma.lululemon.jobs.lululemon.overview.scrapeProductOverviewByType
import de.ma.lululemon.jobs.pages.ScrapeService
import de.ma.lululemon.jobs.lululemon.product.scrapeProduct
import kotlinx.coroutines.runBlocking
import javax.enterprise.context.ApplicationScoped


@ApplicationScoped
class ScrapeScheduldedTask(
    private val scrapeService: ScrapeService
) {

    fun getProducts() = runBlocking {}

    fun analyseProduct() = runBlocking {


    }

    //@Scheduled(every = "1m", identity = "my-task")
    fun doSomething() = runBlocking {
        analyseProduct()
    }

}
