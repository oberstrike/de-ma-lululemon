package de.ma.lululemon.jobs.pages.lululemon

import de.ma.lululemon.api.domain.monitor.product.ProductCreateDTO
import javax.enterprise.context.ApplicationScoped

@ApplicationScoped
class LululemonUrlGenerator {

    private val baseUrl: String = "https://www.lululemon.de/de-de/p/%s/%s.html?dwvar_%s_size=%s&dwvar_%s_color=%s"

    fun createUrl(product: ProductCreateDTO): String {
        return baseUrl.format(product.name, product.id, product.id, product.size, product.id, product.color)
    }

}