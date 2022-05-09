package de.ma.lululemon.jobs.lululemon.product

import de.ma.lululemon.jobs.lululemon.Page
import de.ma.lululemon.jobs.pages.ScrapeService
import it.skrape.core.htmlDocument
import it.skrape.fetcher.Result
import it.skrape.selects.attribute
import it.skrape.selects.html5.*


// 1. Parameter: name of product, 2. Parameter: id of product, 3. Parameter: id of product 4. Paramter color
const val url = "https://www.lululemon.de/de-de/p/%s/%s.html?dwvar_prod8%s_color=%s"

fun createUrl(name: String, id: String, color: String): String {
    return url.format(name, id, id, color)
}


internal fun Result.productScrape(): ProductPageModel {
    return htmlDocument {
        val colorGroups = div {
            withClass = "color-group"
            findAll {
                filter { colorGroup ->
                    val isPresentx = colorGroup.span {
                        withClass = "color"
                        try {
                            findFirst {
                                isPresent
                            }
                        } catch (e: Exception) {
                            false
                        }
                    }
                    isPresentx
                }.map { colorGroup ->
                    val price = colorGroup.span {
                        withClass = "markdown-prices"
                        findFirst {
                            ownText.trim()
                                .replace("€", "")
                                .replace(",", ".")
                                .toFloatOrNull() ?: 0.0f
                        }
                    }

                    val colors = colorGroup.button {
                        withClass = "swatch-circle-container"
                        findAll {
                            this.map {
                                attribute("data-attr-value")
                            }
                        }
                    }
                    ColorGroup(colors, price)
                }
            }
        }

        val sizes = span {
            withClass = "size-btns"
            findAll {
                this.map { sizeBtn ->
                    val name = sizeBtn.input {
                        findFirst {
                            this.id
                        }
                    }
                    val available = sizeBtn.input {
                        findFirst {
                            !attributeKeys.contains("disabled")
                        }
                    }

                    Size(name, available)
                }
            }
        }
        ProductPageModel(
            Product(colorGroups, sizes)
        )
    }
}

suspend fun ScrapeService.scrapeProduct(
    name: String,
    id: String,
    color: String
): ProductPageModel {
    //return fetch {
    //    Page(createUrl(name, id, color), Result::productScrape)
    //}
    throw NotImplementedError()
}
