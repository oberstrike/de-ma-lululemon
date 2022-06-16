package de.ma.tracker.domain.base.exception

abstract class DomainException(msg: String) : RuntimeException(
    msg
) {

}