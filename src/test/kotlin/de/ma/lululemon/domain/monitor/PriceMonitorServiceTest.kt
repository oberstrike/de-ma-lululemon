package de.ma.lululemon.domain.monitor




import org.junit.jupiter.api.extension.ExtendWith

//@ExtendWith(MockKExtension::class)
class PriceMonitorServiceTest {
/*

    @MockK
    lateinit var priceMonitorOrderRepository: PriceMonitorOrderRepository

    @InjectMockKs
    lateinit var priceMonitorService: PriceMonitorService


    @Test
    fun createOrderWithQuantityTest() {

        every { priceMonitorOrderRepository.persist(any<PriceMonitorOrderEntity>()) } answers { nothing }

        val prodName = "built-to-move-long-boxer-7inch-3-pack"
        val prodId = "prod10641597"
        val prodColor = "4310"
        val prodSize = "L"

        val urlWithQuantity =
            "https://www.lululemon.de/en-de/p/$prodName/$prodId.html?dwvar_prod10641597_color=$prodColor&dwvar_prod10641597_size=$prodSize&quantity=1"

        val createOrder =
            priceMonitorService.createByUrl(urlWithQuantity)

        verify(exactly = 1) { priceMonitorOrderRepository.persist(any<PriceMonitorOrderEntity>()) }
    }

    @Test
    fun createOrderWithoutQuantityTest() {
        every { priceMonitorOrderRepository.persist(any<PriceMonitorOrderEntity>()) } answers { nothing }

        val prodName = "built-to-move-long-boxer-7inch-3-pack"
        val prodId = "prod10641597"
        val prodColor = "4310"
        val prodSize = "L"

        val urlWithQuantity =
            "https://www.lululemon.de/en-de/p/$prodName/$prodId.html?dwvar_prod10641597_color=$prodColor&dwvar_prod10641597_size=$prodSize"

        val createOrder = priceMonitorService.createByUrl(urlWithQuantity)

        verify(exactly = 1) { priceMonitorOrderRepository.persist(any<PriceMonitorOrderEntity>()) }

    }*/
}