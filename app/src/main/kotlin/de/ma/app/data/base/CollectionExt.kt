package de.ma.app.data.base

infix fun <T> T.`is not content of`(collection: Collection<T>): Boolean {
    return !collection.contains(this)
}