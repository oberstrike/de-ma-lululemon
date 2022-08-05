package de.ma.lululemon

import org.jsoup.nodes.Document

class LululemonProductPage(private val document: Document) : IProductPage {

    private val model = productPageModel()


    override fun isPageNotFound(): Boolean {
        return document.select("h1[class=hero-title]").isNotEmpty()
    }


    override fun size(sizeName: String): ArticleSize {

        return model.articleSizes.firstOrNull { it.name == sizeName }
            ?: throw IllegalArgumentException("Size $sizeName not found")

    }

    override fun price(): Float {

        return model.colorGroups.first { it.selected }.price
    }


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
                        Color(it.attr("data-attr-value"), selected = it.hasClass("selected"))
                    }

                val selected = colorGroupElement.select("button")
                    .mapNotNull { it.hasClass("selected") }
                    .reduce { acc, element ->
                        acc || element
                    }


                LululemonColorGroup(lululemonColors, price, selected)
            }
    }

    private fun sizes(): List<ArticleSize> {
        return document.select("span[class=size-btns]").mapNotNull { sizeSpan ->
            sizeSpan.select("input").firstOrNull()!!
        }.map { sizeInput ->
            ArticleSize(
                sizeInput.id(),
                !sizeInput.hasAttr("disabled")
            )
        }
    }


    private fun productPageModel(): LululemonProductPageModel {

        return LululemonProductPageModel(
            colorGroups(), sizes()

        )
    }


}


data class LululemonProductPageModel(
    val colorGroups: List<LululemonColorGroup>,
    val articleSizes: List<ArticleSize>
)

data class LululemonColorGroup(
    val colors: List<Color>,
    val price: Float,
    val selected: Boolean
)