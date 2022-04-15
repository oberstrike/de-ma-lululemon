package de.ma.lululemon.jobs.pages.overview

import de.ma.lululemon.jobs.pages.Page
import de.ma.lululemon.jobs.pages.ScrapeTask
import it.skrape.core.htmlDocument
import it.skrape.fetcher.Result
import it.skrape.selects.attribute
import it.skrape.selects.html5.a
import it.skrape.selects.html5.div
import it.skrape.selects.html5.span

val url = "https://www.lululemon.de/en-de/c/mens/shorts?prefn1=refinementColor&prefv1=Black&sz=100"


internal fun Result.overviewScrape(): OverviewPageModel {
    return htmlDocument {
        val products = mutableListOf<ProductOverview>()


        div {
            withClass = "product"
            findAll {
                forEach { productElement ->
                    val name = productElement.div {
                        withAttribute = "itemprop" to "name"
                        findFirst {
                            a {
                                findFirst {
                                    ownText
                                }
                            }
                        }
                    }

                    val prices = productElement.div {
                        withClass = "sales"
                        findAll {
                            map{ salesDocument ->
                                salesDocument.span {
                                    withClass = "value"
                                    findFirst {
                                        attribute("content")
                                    }

                                }

                            }
                        }

                    }

                    println("Name: $name, Prices: $prices")
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