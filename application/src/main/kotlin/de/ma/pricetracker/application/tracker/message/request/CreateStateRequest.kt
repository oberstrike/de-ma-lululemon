package de.ma.pricetracker.application.tracker.message.request

interface CreateStateRequest {
    val product: Long
    val searchCount: Long
}