package de.ma.app.data.state

import de.ma.tracker.domain.state.State
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase
import java.util.*
import javax.enterprise.context.ApplicationScoped

@ApplicationScoped
class StateRepository : PanacheRepositoryBase<StateEntity, UUID> {
    fun findByUserId(userId: UUID): List<StateEntity> {
        return find("user.id = ?1", userId).list()
    }
}