package de.ma.lululemon.jobs

import de.ma.lululemon.api.domain.monitor.PriceMonitorService
import io.quarkus.test.junit.QuarkusTest
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import javax.inject.Inject

@QuarkusTest
class ScrapeScheduldedTaskTest(
    private val scrapeScheduldedTask: ScrapeScheduldedTask,
    private val priceMonitorService: PriceMonitorService
) {

    @Test
    fun job() {
        priceMonitorService.createByUrl("https://www.lululemon.de/en-de/p/abc-jogger/prod8530240.html?dwvar_prod8530240_size=M&_color=32476")

        scrapeScheduldedTask.job()
    }
}