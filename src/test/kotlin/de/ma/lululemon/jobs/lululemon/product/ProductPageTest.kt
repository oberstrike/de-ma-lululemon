package de.ma.lululemon.jobs.lululemon.product

import de.ma.lululemon.jobs.pages.ProductPage
import org.jsoup.Jsoup
import org.junit.jupiter.api.Test

class ProductPageTest {

    @Test
    fun colorGroups() {
        val document = Jsoup.connect("https://www.lululemon.de/de-de/p/always-in-motion-boxers-5er-pack/prod9660102.html?dwvar_prod9660102_color=54688")
            .get()

        val productPage = ProductPage(document)

        val productPageModel = productPage.productPageModel()

        println(productPageModel)

    }
}