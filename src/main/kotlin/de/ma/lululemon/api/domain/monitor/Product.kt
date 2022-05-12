package de.ma.lululemon.api.domain.monitor

import de.ma.lululemon.api.domain.entry.Entry


class Product{

    lateinit var id: String

    lateinit var color: String

    lateinit var size: String

    lateinit var name: String

    lateinit var url: String

    var entries: MutableSet<Entry> = mutableSetOf()

    fun addEntry(entry: Entry) {
        this.entries.add(entry)
    }
}

