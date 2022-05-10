package de.ma.lululemon.jobs.pages

import org.jsoup.nodes.Document


class ProductPage(private val document: Document) {
    private fun colorGroups(): List<ColorGroup> {
        val colorGroupElements = document.select("div[class=color-group]")

        return colorGroupElements
            .mapNotNull { colorGroupElement ->
                val price =
                    colorGroupElement.select("span[class=markdown-prices]")
                        .firstOrNull()
                        ?.text()
                        ?.trim()
                        ?.replace("€", "")
                        ?.replace(",", ".")?.toFloatOrNull() ?: 0.0f

                val colors = colorGroupElement.select("button")
                    .mapNotNull {
                        Color(it.attr("data-attr-value"), selected = it.hasClass("selected"))
                    }

                ColorGroup(colors, price)
            }
    }

    private fun sizes(): List<Size> {
        return document.select("span[class=size-btns]").mapNotNull { sizeSpan ->
            sizeSpan.select("input").firstOrNull()!!
        }.map { sizeInput ->
            Size(
                sizeInput.id(),
                !sizeInput.hasAttr("disabled")
            )
        }
    }

    fun productPageModel(): ProductPageModel {

        return ProductPageModel(
            colorGroups(), sizes()

        )
    }
}