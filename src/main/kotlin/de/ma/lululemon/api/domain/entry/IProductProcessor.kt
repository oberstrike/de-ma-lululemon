package de.ma.lululemon.api.domain.entry

import de.ma.lululemon.api.domain.monitor.Product
import de.ma.lululemon.jobs.pages.IPageModel

interface IProductProcessor {
    fun process(product: Product): IPageModel?
    fun isShop(shopName: String): Boolean
}