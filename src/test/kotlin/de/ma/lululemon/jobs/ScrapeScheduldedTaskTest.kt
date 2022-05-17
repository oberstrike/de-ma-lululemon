package de.ma.lululemon.jobs

import de.ma.lululemon.api.domain.monitor.PriceMonitorService
import io.quarkus.test.junit.QuarkusTest
import org.junit.jupiter.api.Test

@QuarkusTest
class ScrapeScheduldedTaskTest(
    private val scrapeScheduledTask: ScrapeScheduledTask,
    private val priceMonitorService: PriceMonitorService
) {

    @Test
    fun job() {
        scrapeScheduledTask.job()
    }
}