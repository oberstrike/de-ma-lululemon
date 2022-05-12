package de.ma.lululemon.api.domain.monitor

import org.bson.types.ObjectId
import javax.enterprise.context.ApplicationScoped

@ApplicationScoped
class PriceMonitorService(
    private val priceMonitorOrderRepository: PriceMonitorOrderRepository
) {

    fun createByProduct(productCreateDTO: ProductCreateDTO): PriceMonitorOrderEntity {
        val product = productCreateDTO.toProduct()

        val exists = priceMonitorOrderRepository.find(
            "product.name = ?1 " +
                    ", product.id = ?2 " +
                    ", product.size = ?3 " +
                    ", product.color = ?4",

            product.name,
            product.id,
            product.size,
            product.color
        ).firstResult() != null

        if (exists) {
            throw IllegalArgumentException("Product already exists")
        }

        product.url = createUrl(
            product.name,
            product.id,
            product.size,
            product.color
        )


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
        val prodColor = url.variable("color")!!
        val prodSize = url.variable("size") ?: "M"

        //create product
        val product = ProductCreateDTO(
            prodId = prodId,
            prodColor = prodColor,
            prodSize = prodSize,
            prodName = prodName
        )

        //create price monitor order
        return createByProduct(product)
    }

    fun getAll(): List<PriceMonitorOrderEntity> {
        return priceMonitorOrderRepository.findAll().list()
    }


    fun String.variable(varName: String): String? {
        if (!contains(varName)) {
            return null
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

    private val baseUrl: String = "https://www.lululemon.de/de-de/p/%s/%s.html?dwvar_%s_size=%s&_color=%s"

    private fun createUrl(productName: String, productId: String, prodSize: String, productColor: String): String {
        return baseUrl.format(productName, productId, productId, prodSize, productColor)
    }

    fun deleteById(id: String) {
        val objectId = ObjectId(id)

        priceMonitorOrderRepository.findById(objectId) ?: throw IllegalArgumentException("Order with id $id not found")

        priceMonitorOrderRepository.deleteById(objectId)
    }

}