package de.ma.lululemon.api.domain.monitor

import de.ma.lululemon.jobs.pages.common.IShopService
import de.ma.lululemon.api.domain.monitor.product.toProduct
import org.bson.types.ObjectId
import javax.enterprise.context.ApplicationScoped
import javax.enterprise.inject.Instance

@ApplicationScoped
class PriceMonitorService(
    private val priceMonitorOrderRepository: PriceMonitorOrderRepository,
    private val shopServices: Instance<IShopService>
) {

    fun create(priceMonitorCreate: PriceMonitorCreate): PriceMonitorOrderEntity {

        val productCreateDTO = priceMonitorCreate.productCreate

        val product = productCreateDTO.toProduct()

        if (priceMonitorOrderRepository.existsByProduct(product)) {
            throw IllegalArgumentException("Product already exists")
        }

        val urlGenerator = shopServices.firstOrNull { it.isShop(priceMonitorCreate.shopType) }
            ?: throw IllegalArgumentException("Shop not supported: ${priceMonitorCreate.shopType}")

        product.url = urlGenerator.createUrl(productCreateDTO)


        val orderEntity = PriceMonitorOrderEntity(
            product = product,
            shopType = priceMonitorCreate.shopType
        )


        priceMonitorOrderRepository.persist(orderEntity)
        return orderEntity
    }


    fun getAll(): List<PriceMonitorOrderEntity> {
        return priceMonitorOrderRepository.findAll().list()
    }


    fun getAllOrders(): List<PriceMonitorOrderDTO> {
        return priceMonitorOrderRepository.findAll().list().map {
            it.toDTO()
        }
    }

    fun save(priceMonitorOrderEntity: PriceMonitorOrderEntity) {
        priceMonitorOrderRepository.update(priceMonitorOrderEntity)
    }


    fun deleteById(id: String) {
        val objectId = ObjectId(id)

        priceMonitorOrderRepository.findById(objectId) ?: throw IllegalArgumentException("Order with id $id not found")

        priceMonitorOrderRepository.deleteById(objectId)
    }

    fun getById(id: String): PriceMonitorOrderEntity {
        return priceMonitorOrderRepository.findById(ObjectId(id))
            ?: throw IllegalArgumentException("Order with id $id not found")
    }



}
