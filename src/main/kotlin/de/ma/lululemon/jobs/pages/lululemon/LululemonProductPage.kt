package de.ma.lululemon.jobs.pages.lululemon

import org.jsoup.nodes.Document


class LululemonProductPage(private val document: Document) {
    private fun colorGroups(): List<LululemonColorGroup> {
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

                val lululemonColors = colorGroupElement.select("button")
                    .mapNotNull {
                        LululemonColor(it.attr("data-attr-value"), selected = it.hasClass("selected"))
                    }

                LululemonColorGroup(lululemonColors, price)
            }
    }

    private fun sizes(): List<LululemonSize> {
        return document.select("span[class=size-btns]").mapNotNull { sizeSpan ->
            sizeSpan.select("input").firstOrNull()!!
        }.map { sizeInput ->
            LululemonSize(
                sizeInput.id(),
                !sizeInput.hasAttr("disabled")
            )
        }
    }

    fun pageNotFound(): Boolean{
        return document.select("h1[class=hero-title]").isNotEmpty()
    }

    fun productPageModel(): LululemonProductPageModel {

        return LululemonProductPageModel(
            colorGroups(), sizes()

        )
    }
}