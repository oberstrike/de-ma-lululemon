package de.ma.tracker.domain.base.exception

class NotFoundException(msg: String?) : DomainException(
    msg ?: "Not found"
) {
}