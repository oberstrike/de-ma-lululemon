package de.ma.app.utils.sql

import io.quarkus.test.junit.callback.QuarkusTestBeforeClassCallback
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking

class SqlQuarkusTestBeforeClassCallback : QuarkusTestBeforeClassCallback {

    override fun beforeClass(testClass: Class<*>) = runBlocking(
        Dispatchers.IO
    ) {
        val sql = testClass.annotations.filterIsInstance(Sql::class.java).firstOrNull()

        if (sql != null) {
            val targetFiles = sql.before
            for (targetFile in targetFiles) {
                SqlFileProcessor.processTargetFile(targetFile)
            }
        }

    }
}