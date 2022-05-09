package de.ma.lululemon.jobs.lululemon.overview

import de.ma.lululemon.jobs.lululemon.Page
import de.ma.lululemon.jobs.pages.ScrapeService
import it.skrape.core.htmlDocument
import it.skrape.fetcher.Result
import it.skrape.selects.html5.a
import it.skrape.selects.html5.div
import it.skrape.selects.html5.span
import java.time.LocalDateTime

const val url = "https://www.lululemon.de/de-de/c/maenner/%s?sz=100"

internal fun Result.productOverviewScrape(): ProductOverviewPageModel {
    return htmlDocument {
        val products = div {
            withClass = "product"
            findAll {
                map { productElement ->
                    val prodId = productElement.attribute("data-pid")
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
                    val link = productElement.div {
                        withAttribute = "itemprop" to "name"
                        findFirst {
                            a {
                                findFirst {

                                    val productLink = attribute("href")
                                    // extract %s from "/de-de/p/%s/xx.html"
                                    productLink.split("/")[3]
                                }
                            }
                        }
                    }

                    val prices = productElement.div {
                        withClass = "sales"
                        findAll {
                            map { salesDocument ->
                                salesDocument.span {
                                    withClass = "value"
                                    findFirst {
                                        attribute("content")
                                    }

                                }

                            }
                        }
                    }.map { it.toFloatOrNull() ?: 0.0f }

                    ProductOverview(
                        prodId,
                        name,
                        prices,
                        LocalDateTime.now(),
                        link
                    )
                }

            }
        }

        ProductOverviewPageModel(products)
    }

}

suspend fun ScrapeService.scrapeProductOverviewByType(productType: String): ProductOverviewPageModel {
   // return fetch {
  //      Page(url.format(productType), Result::productOverviewScrape)
  //  }
    throw NotImplementedError()
}