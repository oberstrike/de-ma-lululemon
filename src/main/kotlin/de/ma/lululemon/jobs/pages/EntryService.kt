package de.ma.lululemon.jobs.pages


import de.ma.lululemon.api.domain.entry.Entry
import de.ma.lululemon.api.domain.monitor.Product
import org.jsoup.Jsoup
import java.time.LocalDateTime
import javax.enterprise.context.ApplicationScoped


@ApplicationScoped
class EntryService {


    fun createEntry(product: Product): Entry? {

        val url = product.url
        val document = Jsoup.connect(url).get()

        val productPage = ProductPage(document)

        val isPageNotFound = productPage.pageNotFound()
        if (isPageNotFound) {
            return null
        }

        val productPageModel = productPage.productPageModel()

        val selectedColorGroup = productPageModel
            .colorGroups.first { colorGroup -> colorGroup.colors.any { it.name == product.color } }

        return Entry().apply {
            this.price = selectedColorGroup.price
            this.timestamp = LocalDateTime.now()
        }

    }


}


