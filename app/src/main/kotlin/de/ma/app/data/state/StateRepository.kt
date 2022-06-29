package de.ma.app.data.state

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase
import java.util.*
import javax.enterprise.context.ApplicationScoped

@ApplicationScoped
class StateRepository : PanacheRepositoryBase<StateEntity, UUID> {
    fun findByProductId(productId: UUID): List<StateEntity> {
        return find(
            "select distinct s from state s left join fetch s.product where s.product.id = ?1 ",
            productId
        ).list()
    }

    fun findByIdProductFetched(id: UUID): StateEntity {
        return find(
            "select distinct s from state s left join fetch s.product where s.id = ?1 ",
            id
        ).firstResult()
    }
}