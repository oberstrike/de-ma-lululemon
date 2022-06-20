package de.ma.app.utils.sql

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.hibernate.SessionFactory
import java.io.BufferedReader
import java.io.FileNotFoundException
import java.io.InputStreamReader
import javax.enterprise.inject.spi.CDI

object SqlFileProcessor {
    suspend fun processTargetFile(targetFile: String) = withContext(Dispatchers.IO) {
        println("Processing file: $targetFile")

        val mutinySessionFactory = sessionFactory()

        val inputStream =
            this::class.java.getResource(targetFile)?.openStream() ?: throw FileNotFoundException(
                "File not found: $targetFile"
            )

        val bufferedReader = BufferedReader(InputStreamReader(inputStream))

        var nextCommand = ""
        var isDo = false

        bufferedReader.forEachLineAsync { line ->
            if (line.startsWith("--")) {
                return@forEachLineAsync

            }
            nextCommand += line

            if( isDo &&  !line.startsWith("END \$\$;")) {
                return@forEachLineAsync
            }

            if(line.startsWith("DO")) {
                isDo = true
                return@forEachLineAsync
            }

            if (line.endsWith(";") || line.endsWith("END \$\$;")) {
                mutinySessionFactory.openSession().use { session ->
                    session.createNativeQuery(nextCommand).executeUpdate()
                }
                isDo = false
                nextCommand = ""
            }
        }
        println("Processed file: $targetFile")
    }

    private fun sessionFactory(): SessionFactory {
        try {
            return CDI.current().select(SessionFactory::class.java).get()
        } catch (e: Exception) {
            throw RuntimeException("Could not get Session Factory", e)
        }
    }

    private suspend fun BufferedReader.forEachLineAsync(action: suspend (String) -> Unit) {
        forEachLine {
            runBlocking {
                action(it)
            }
        }
    }

}