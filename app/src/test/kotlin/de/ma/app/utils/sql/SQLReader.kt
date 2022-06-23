package de.ma.app.utils.sql

import java.io.BufferedReader
import java.io.Reader

class SQLReader(
    private val bufferedReader: BufferedReader
) : Reader() {

    fun readQuery(): String? {
        var stop = false
        val query = StringBuilder()
        var isDoOperation = false

        while (!stop) {

            val line = bufferedReader.readLine() ?: break

            if (line.startsWith("--")) {
                continue
            }

            query.append(line)

            if (isDoOperation && !line.startsWith("END \$\$;")) {
                continue
            }

            if (line.startsWith("DO")) {
                isDoOperation = true
            }

            if (line.endsWith(";") || line.endsWith("END \$\$;")) {
                stop = true
            }
        }

        return if (query.isEmpty()) {
            null
        } else query.toString()
    }

    fun readQueries(): List<String> {
        val queries = mutableListOf<String>()
        var query: String?
        while (true) {
            query = readQuery()
            if (query == null) {
                break
            }
            queries.add(query)
        }
        return queries
    }

    override fun read(cbuf: CharArray, off: Int, len: Int): Int {
        return bufferedReader.read(cbuf, off, len)
    }

    override fun close() {
        bufferedReader.close()
    }
}