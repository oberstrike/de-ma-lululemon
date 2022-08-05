package de.ma.app.data.product

import de.ma.tracker.domain.product.message.ProductOverview
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase
import java.util.*
import javax.enterprise.context.ApplicationScoped

@ApplicationScoped
class ProductRepository : PanacheRepositoryBase<ProductEntity, UUID> {
    fun findByTracked(isTracked: Boolean): List<ProductOverview> {
        return find(
            "select p from product p where p.is_tracked = ?1 ",
            isTracked
        ).list<ProductEntity>()
            .mapNotNull { it.toOverview() }
    }

}