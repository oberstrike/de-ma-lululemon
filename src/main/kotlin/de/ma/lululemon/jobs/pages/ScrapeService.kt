package de.ma.lululemon.jobs.pages

import de.ma.lululemon.api.domain.product.ProductShowDTO
import de.ma.lululemon.jobs.lululemon.product.scrapeProduct
import javax.enterprise.context.ApplicationScoped

@ApplicationScoped
class ScrapeService(
    private val scrapeGateway: ScrapeGateway
) {

   suspend fun scrapeProduct(product: ProductShowDTO){
       scrapeGateway.scrape {

       }
   }

}