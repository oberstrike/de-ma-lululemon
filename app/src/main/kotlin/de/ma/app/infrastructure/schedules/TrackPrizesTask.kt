package de.ma.app.infrastructure.schedules

import de.ma.pricetracker.api.product.TrackPrizeUseCase
import io.quarkus.scheduler.Scheduled
import javax.enterprise.context.ApplicationScoped
import javax.transaction.Transactional

@ApplicationScoped
class TrackPrizesTask(
    private val trackPrizeUseCase: TrackPrizeUseCase
) {

    @Transactional
    @Scheduled(every = "5000s", identity = "track-price-job")
    fun schedule() {
        trackPrizeUseCase.execute()
    }
}