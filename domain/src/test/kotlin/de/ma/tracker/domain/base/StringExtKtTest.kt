package de.ma.tracker.domain.base

import org.amshove.kluent.shouldBe
import org.junit.jupiter.api.Test

class StringExtKtTest {

    @Test
    fun isUrlTest() {
        "http://www.google.com".isUrl() shouldBe true
        "asdf".isUrl() shouldBe false
        "http://www.google.com/asdf".isUrl() shouldBe true
        "https://lululemon.com".isUrl() shouldBe true
        "https://lululemon.com/asdf".isUrl() shouldBe true
        "www.google.com".isUrl() shouldBe false
    }
}