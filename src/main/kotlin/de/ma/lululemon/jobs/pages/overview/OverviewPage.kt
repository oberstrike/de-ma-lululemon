package de.ma.lululemon.jobs.pages.overview

import de.ma.lululemon.jobs.pages.Page
import de.ma.lululemon.jobs.pages.ScrapeTask
import it.skrape.core.htmlDocument
import it.skrape.fetcher.Result
import it.skrape.selects.attribute
import it.skrape.selects.html5.div

val url = "https://www.lululemon.de/en-de/c/mens/shorts?prefn1=refinementColor&prefv1=Black&sz=100"


internal fun Result.overviewScrape(): OverviewPageModel {
    return htmlDocument {
        val products = mutableListOf<ProductOverview>()
        div {
            withClass = "product"
            findAll {
                forEach { product ->
                    //get pid
                    val pid = product.attribute("data-pid")

                    //get price
                    withAttribute = "itemprop" to "name" + ""
                    val name = findFirst {
                        this.className
                    }


                    products.add(
                        ProductOverview(
                            pid = pid,
                            name = name,
                            price = 0.0f
                        )
                    )
                }
            }
        }



        OverviewPageModel(products)
    }

}

suspend fun ScrapeTask.overview(): OverviewPageModel {
    return fetch {
        Page(url, Result::overviewScrape)
    }
}