package de.ma.lululemon.api.domain.entry


import de.ma.lululemon.api.domain.monitor.Product
import de.ma.lululemon.jobs.pages.lululemon.LululemonProcessor
import java.time.LocalDateTime
import javax.enterprise.context.ApplicationScoped


@ApplicationScoped
class EntryService {

    private val processors: List<IProductProcessor> = listOf(LululemonProcessor())

    fun createEntry(
        product: Product,
        shopName: String
    ): Entry? {

        val processor =
            processors.firstOrNull { it.isShop(shopName) } ?: throw IllegalArgumentException("Shop not supported")

        val pageModel =
            processor.process(product) ?: throw IllegalArgumentException("Product ${product.name} could not minitored")

        return Entry().apply {
            this.available = pageModel.available
            this.price = pageModel.price
            this.timestamp = LocalDateTime.now()
        }
    }


}


