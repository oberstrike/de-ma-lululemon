package de.ma.app.data.shop.lululemon

import de.ma.lululemon.IProductParam

data class ProductParamImpl(
    override val id: String,
    override val size: String,
    override val color: String
): IProductParam
