package de.ma.lululemon.jobs.pages.underarmour

import de.ma.lululemon.api.domain.monitor.product.Product
import de.ma.lululemon.jobs.pages.IPageModel
import de.ma.lululemon.jobs.pages.PageModel
import org.jsoup.Jsoup
import javax.enterprise.context.ApplicationScoped

@ApplicationScoped
class UnderArmourProcessor {

    fun process(product: Product): IPageModel? {

        val url = product.url
        val document = Jsoup.connect(url).get()

        val underArmourProductPage = UnderArmourProductPage(document)

        val price = underArmourProductPage.price()
        val size = underArmourProductPage.size(product.size)

        return PageModel(
            price,
            size.available
        )
    }


}