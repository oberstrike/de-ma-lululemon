package de.ma.lululemon.jobs

import de.ma.lululemon.api.domain.monitor.PriceMonitorService
import de.ma.lululemon.jobs.pages.EntryService
import io.quarkus.scheduler.Scheduled
import kotlinx.coroutines.runBlocking
import javax.enterprise.context.ApplicationScoped


@ApplicationScoped
class ScrapeScheduldedTask(
    private val entryService: EntryService,
    private val priceMonitorService: PriceMonitorService
) {

    fun error() : Nothing = throw Exception("Error")

    @Scheduled(cron = "0 0 12 * * ?")
    fun job(){

        val priceMonitorOrderEntities = priceMonitorService.getAll()

        for(priceMonitorOrderEntity in priceMonitorOrderEntities){

            val entry = entryService.createEntry(priceMonitorOrderEntity.product!!) ?: error()

            priceMonitorOrderEntity.product!!.addEntry(entry)
            priceMonitorService.save(priceMonitorOrderEntity)

        }



    }

}
