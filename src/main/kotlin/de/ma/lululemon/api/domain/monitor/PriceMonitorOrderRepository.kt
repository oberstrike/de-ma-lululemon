package de.ma.lululemon.api.domain.monitor

import de.ma.lululemon.api.domain.monitor.product.Product
import io.quarkus.mongodb.panache.kotlin.PanacheMongoRepository
import javax.enterprise.context.ApplicationScoped

@ApplicationScoped
class PriceMonitorOrderRepository : PanacheMongoRepository<PriceMonitorOrderEntity> {

    fun existsByProduct(product: Product) =
        find(
            "product.name = ?1 " +
                    ", product.id = ?2 " +
                    ", product.size = ?3 " +
                    ", product.color = ?4",

            product.name,
            product.id,
            product.size,
            product.color
        ).firstResult() != null


}