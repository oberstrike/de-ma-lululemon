package de.ma.tracker.domain.product.entity

import de.ma.tracker.domain.product.vo.UrlVO
import org.amshove.kluent.invoking
import org.amshove.kluent.shouldBe
import org.amshove.kluent.shouldThrow
import org.junit.jupiter.api.Test

class UrlVOTest{

    @Test
    fun createTest(){
        val url = "http://www.google.com"
        val urlVO = UrlVO(url)
        url shouldBe urlVO.url
    }

    @Test
    fun createNegativeTest(){
        val url = "Test"
        invoking { UrlVO(url) } shouldThrow IllegalArgumentException::class
    }

}