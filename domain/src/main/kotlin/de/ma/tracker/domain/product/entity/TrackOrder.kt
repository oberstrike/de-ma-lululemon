package de.ma.tracker.domain.product.entity

import de.ma.tracker.domain.base.IBaseEntity

interface TrackOrder : IBaseEntity {

    var target: Product

}