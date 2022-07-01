package de.ma.app.data.shop

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase
import java.util.*
import javax.enterprise.context.ApplicationScoped

@ApplicationScoped
class ShopRepository : PanacheRepositoryBase<ShopEntity, UUID> {
    fun findByProductId(productId: UUID): ShopEntity? {
        return find("select distinct s from shop s left join fetch s.product where s.product.id = ?1").firstResultOptional<ShopEntity>()
            .orElse(null)
    }

    fun existsByName(name: String): Boolean {
        return find("name = ?1", name).firstResultOptional<ShopEntity>()
            .orElse(null) != null
    }
}