package de.ma.app.utils.sql

@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
annotation class Sql(
    val before: Array<String> = [],
    val after: Array<String> = [],
)
