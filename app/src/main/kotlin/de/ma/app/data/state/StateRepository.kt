package de.ma.app.data.state

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase
import java.util.*
import javax.enterprise.context.ApplicationScoped
import javax.persistence.NamedQueries
import javax.persistence.NamedQuery

@ApplicationScoped
class StateRepository : PanacheRepositoryBase<StateEntity, UUID> {
    fun findByProductId(productId: UUID): List<StateEntity> {
        return find("product.id = ?1", productId).list()
    }
}