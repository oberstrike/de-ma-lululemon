package de.ma.app.data.product

import de.ma.app.data.state.StateEntity
import de.ma.tracker.domain.product.Product
import de.ma.tracker.domain.state.message.StateShow
import io.quarkus.hibernate.orm.panache.PanacheRepository
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase
import java.util.*
import javax.enterprise.context.ApplicationScoped

@ApplicationScoped
class ProductRepository: PanacheRepositoryBase<ProductEntity, UUID> {
    fun findByShopId(id: UUID): List<ProductEntity>{
        return find("shop.id = ?1", id).list()
    }
}