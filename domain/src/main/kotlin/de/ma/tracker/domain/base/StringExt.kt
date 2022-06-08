package de.ma.tracker.domain.base


internal fun String.isPattern(pattern: String): Boolean {
    return this.matches(Regex(pattern))
}

internal fun checkStringIsHttpUrl(url: String): Boolean {
    return url.isPattern("^(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]")
}

fun String.isUrl(): Boolean {
    return checkStringIsHttpUrl(this)
}