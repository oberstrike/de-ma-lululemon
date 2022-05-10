package de.ma.lululemon.jobs.pages


import de.ma.lululemon.api.domain.monitor.Product
import org.jsoup.Jsoup
import java.time.LocalDateTime
import javax.enterprise.context.ApplicationScoped


@ApplicationScoped
class EntryService {

    private val baseUrl: String = "https://www.lululemon.de/de-de/p/%s/%s.html?dwvar_%s_size=%s&_color=%s"

    private fun createUrl(productName: String, productId: String, prodSize: String, productColor: String): String {
        return baseUrl.format(productName, productId, productId, prodSize, productColor)
    }

    fun createEntry(product: Product): Entry {

        val url = createUrl(product.prodName, product.prodId, product.prodSize, product.prodColor)
        val document = Jsoup.connect(url).get()

        val productPage = ProductPage(document)

        val productPageModel = productPage.productPageModel()

        val selectedColorGroup = productPageModel
            .colorGroups.first { colorGroup -> colorGroup.colors.any { it.name == product.prodColor } }

        return Entry().apply {
            this.price = selectedColorGroup.price
            this.timestamp = LocalDateTime.now()
        }

    }


}


