package de.ma.lululemon.api.domain.monitor

import de.ma.lululemon.jobs.pages.Entry


class Product{

    lateinit var prodId: String

    lateinit var prodColor: String

    lateinit var prodSize: String

    lateinit var prodName: String

    var entries: MutableSet<Entry> = mutableSetOf()

    fun addEntry(entry: Entry) {
        this.entries.add(entry)
    }
}

