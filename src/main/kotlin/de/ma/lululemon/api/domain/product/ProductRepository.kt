package de.ma.lululemon.api.domain.product

import io.quarkus.mongodb.panache.kotlin.PanacheMongoRepository
import javax.enterprise.context.ApplicationScoped

@ApplicationScoped
class ProductRepository : PanacheMongoRepository<ProductEntity> {

}