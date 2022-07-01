package de.ma.app.infrastructure.schedules

import de.ma.pricetracker.api.product.AddStateToProductUseCase
import io.quarkus.scheduler.Scheduled
import javax.enterprise.context.ApplicationScoped
import javax.transaction.Transactional

@ApplicationScoped
class TrackerTask(
    private val addStateToProductUseCase: AddStateToProductUseCase
) {

    @Transactional
    @Scheduled(every = "10s", identity = "track-price-job")
    fun schedule() {


    }
}