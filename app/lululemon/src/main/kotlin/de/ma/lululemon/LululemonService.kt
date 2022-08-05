package de.ma.lululemon

import de.ma.tracker.domain.state.message.StateCreate
import org.jsoup.Jsoup
import java.time.LocalDateTime

class LululemonService {

    private val baseUrl: String = "https://www.lululemon.de/de-de/p/%s.html?dwvar_%s_size=%s&dwvar_%s_color=%s"

    fun createState(productParam: IProductParam): StateCreate {
        val document = Jsoup.connect(createUrl(productParam)).get()

        val productPage = LululemonProductPage(document)

        val pageNotFound = productPage.isPageNotFound()

        val articleSize = productPage.size(productParam.size)

        val isNotRightSize = articleSize.name != productParam.size

        if (isNotRightSize || !articleSize.available || pageNotFound) {
            return StateCreateDTO(
                LocalDateTime.now(),
                0.0F
            )
        }

        return StateCreateDTO(
            LocalDateTime.now(),
            productPage.price()
        )

    }

    private fun createUrl(product: IProductParam): String {
        return baseUrl.format(product.id, product.id, product.size, product.id, product.color)
    }
}

