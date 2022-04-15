package de.ma.lululemon.jobs

import io.quarkus.test.junit.QuarkusTest
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import javax.inject.Inject

@QuarkusTest
class ScrapeScheduldedTaskTest {

    @Inject
    lateinit var scrapeScheduldedTask: ScrapeScheduldedTask

    @Test
    fun doSomething() {
        scrapeScheduldedTask.doSomething()
    }
}