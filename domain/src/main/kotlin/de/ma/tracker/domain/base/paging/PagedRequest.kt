package de.ma.tracker.domain.base.paging

interface PagedRequest {
    val page: Int
    val pageSize: Int
}