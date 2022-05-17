package de.ma.lululemon.jobs

import de.ma.lululemon.jobs.pages.common.IShopService
import de.ma.lululemon.api.domain.monitor.PriceMonitorService
import de.ma.lululemon.api.domain.monitor.product.toDTO
import io.quarkus.scheduler.Scheduled
import javax.enterprise.context.ApplicationScoped
import javax.enterprise.inject.Instance


@ApplicationScoped
class ScrapeScheduledTask(
    private val shopServices: Instance<IShopService>,
    private val priceMonitorService: PriceMonitorService
) {

    @Scheduled(cron = "0 0 12 * * ?")
    fun job() {

        val priceMonitorOrderEntities = priceMonitorService.getAll()

        for (orderEntity in priceMonitorOrderEntities) {

            val shopType = orderEntity.shopType
            val shopService = shopServices.firstOrNull { it.isShop(shopType) }?:
                throw IllegalArgumentException("Shop not found: $shopType")

            val state = shopService.getCurrentStateOfProduct(
                orderEntity.product.toDTO()
            ) ?: throw IllegalArgumentException("There was an problem updating ${orderEntity.id.toString()}")

            orderEntity.apply {
                this.product.addState(state)
                this.increaseSearchCount()
            }

            priceMonitorService.save(orderEntity)

        }


    }

}
