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

        val sessionFactory = sessionFactory()

        val inputStream = this::class.java.getResource(targetFile)?.openStream() ?: throw FileNotFoundException(
            "File not found: $targetFile"
        )

        val bufferedReader = SQLReader(BufferedReader(InputStreamReader(inputStream)))

        val queries = bufferedReader.readQueries()

        queries.forEach {
            sessionFactory.openSession().use { session ->
                val transaction = session.beginTransaction()
                session.createNativeQuery(it).executeUpdate()
                transaction.commit()
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




}