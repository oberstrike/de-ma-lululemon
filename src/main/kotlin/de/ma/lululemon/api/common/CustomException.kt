package de.ma.lululemon.api.common

data class CustomException(
    val name: String,
    val code: Int,
    val message: String?
)