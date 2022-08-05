package de.ma.lululemon

import org.junit.jupiter.api.Test

class LululemonServiceTest {

    val lululemonService = LululemonService()

    data class ProductParamImpl(
        override val id: String,
        override val color: String,
        override val size: String
    ): IProductParam

    @Test
    fun createState() {

        val createState = lululemonService.createState(
            ProductParamImpl(
                "prod9200786",
                "0001",
                "L"
            )
        )



    }
}