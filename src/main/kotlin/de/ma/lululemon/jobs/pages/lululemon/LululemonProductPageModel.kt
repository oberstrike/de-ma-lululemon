package de.ma.lululemon.jobs.pages.lululemon

data class LululemonProductPageModel(
    val colorGroups: List<LululemonColorGroup>,
    val sizes: List<LululemonSize>
)

data class LululemonColorGroup(
    val colors: List<LululemonColor>,
    val price: Float
)

data class LululemonColor(
    val name: String,
    val selected: Boolean
)

data class LululemonSize(
    val name: String,
    val available: Boolean
)