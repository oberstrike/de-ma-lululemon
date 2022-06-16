package de.ma.tracker.domain.base.paging

data class PagedList<T>(
    val page: Int,
    val pageCount: Int,
    val items: List<T>
) : List<T> by items

fun <T, U> PagedList<T>.mapPaged(mapper: (T) -> U): PagedList<U> =
    PagedList(page, pageCount, items.map(mapper))

fun <T> aPagedList(page: Int, pageCount: Int, items: List<T>) = PagedList(page, pageCount, items)
