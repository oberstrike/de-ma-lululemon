package de.ma.lululemon.api.domain.monitor

import io.quarkus.mongodb.panache.kotlin.PanacheMongoRepository
import javax.enterprise.context.ApplicationScoped

@ApplicationScoped
class PriceMonitorOrderRepository : PanacheMongoRepository<PriceMonitorOrderEntity> {
}