package de.ma.lululemon.jobs.pages

interface IPageModel {
    val price: Float
    val available: Boolean
}


data class PageModel(
    override val price: Float,
    override val available: Boolean,
) : IPageModel