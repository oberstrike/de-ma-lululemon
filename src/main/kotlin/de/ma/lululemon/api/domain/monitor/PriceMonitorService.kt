package de.ma.lululemon.api.domain.monitor

import javax.enterprise.context.ApplicationScoped

@ApplicationScoped
class PriceMonitorService(
    private val priceMonitorOrderRepository: PriceMonitorOrderRepository
) {

    fun createByProduct(product: Product): PriceMonitorOrderEntity {

        val orderEntity = PriceMonitorOrderEntity(
            product = product
        )

        priceMonitorOrderRepository.persist(orderEntity)
        return orderEntity
    }

    //https://www.lululemon.de/en-de/p/built-to-move-long-boxer-7inch-3-pack/prod10641597.html?dwvar_prod10641597_color=4310&dwvar_prod10641597_size=L&quantity=1
    fun createByUrl(url: String): PriceMonitorOrderEntity {

        //splitt url into parts
        val prodName = url.split("/")[5]
        val prodId = url.between("/", ".html")
        val prodColor = url.variable("color")
        val prodSize = url.variable("size")

        //create product
        val product = Product()

        product.prodName = prodName
        product.prodId = prodId
        product.prodColor = prodColor
        product.prodSize = prodSize
        //create price monitor order
        return createByProduct(product)
    }

    fun getAll(): List<PriceMonitorOrderEntity> {
        return priceMonitorOrderRepository.findAll().list()
    }


    fun String.variable(varName: String): String {
        if (!contains(varName)) {
            return ""
        }

        val index = indexOf(varName)
        val rest = substring(index - 1, length)

        if (rest.contains("&")) {
            return rest.split("&")[0].split("=").last()
        }

        return rest.split("=").last()
    }

    fun String.between(start: String, end: String): String {
        val startIndex = lastIndexOf(start) + start.length
        val endIndex = indexOf(end)
        return substring(startIndex, endIndex)
    }

    fun getAllOrders(): List<PriceMonitorOrderDTO> {
        return priceMonitorOrderRepository.findAll().list().map {
            it.toDTO()
        }
    }

    fun save(priceMonitorOrderEntity: PriceMonitorOrderEntity) {
        priceMonitorOrderRepository.update(priceMonitorOrderEntity)
    }
}