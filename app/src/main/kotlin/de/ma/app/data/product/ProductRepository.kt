package de.ma.app.data.product

import de.ma.tracker.domain.product.Product
import io.quarkus.hibernate.orm.panache.PanacheRepository
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase
import java.util.*
import javax.enterprise.context.ApplicationScoped

@ApplicationScoped
class ProductRepository: PanacheRepositoryBase<ProductEntity, UUID> {

}