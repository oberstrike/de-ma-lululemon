package de.ma.lululemon.jobs.pages.underarmour

import de.ma.lululemon.api.domain.monitor.product.Product
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*

class UnderArmourProcessorTest {

    @Test
    fun process() {
        val underArmourProcessor = UnderArmourProcessor()

        val product = Product()
        product.id = "3025052"
        product.color = "104"
        product.size = "40"
        product.name = "herren_ua_tribase_reign_4_trainingsschuhe"
        product.url = "https://www.underarmour.de/de-de/p/herren_ua_tribase_reign_4_trainingsschuhe/3025052.html?dwvar_3025052_color=104&start=0&breadCrumbLast=Herren"

        val model = underArmourProcessor.process(product)

        println(model)

    }
}