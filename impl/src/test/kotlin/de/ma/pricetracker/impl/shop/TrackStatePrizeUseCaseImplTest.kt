package de.ma.pricetracker.impl.shop

import de.ma.pricetracker.api.product.AddStateToProductUseCase
import de.ma.pricetracker.impl.product.ProductOverviewImpl
import de.ma.pricetracker.impl.shop.data.ShopShowImpl
import de.ma.pricetracker.impl.state.StateCreateImpl
import de.ma.pricetracker.impl.state.StateShowImpl
import de.ma.tracker.domain.product.ProductGateway
import de.ma.tracker.domain.shop.ShopService
import de.ma.tracker.domain.shop.ShopServiceGateway
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.extension.ExtendWith
import java.time.LocalDateTime
import java.util.*

@ExtendWith(MockKExtension::class)
class TrackStatePrizeUseCaseImplTest {

    @InjectMockKs
    lateinit var trackPrizesImpl: TrackPrizeUseCaseImpl


    @MockK
    lateinit var shopServiceGateway: ShopServiceGateway

    @MockK
    lateinit var addStateToProductUseCase: AddStateToProductUseCase

    @MockK
    lateinit var productGateway: ProductGateway

    @MockK
    lateinit var shopService: ShopService

    @Test
    fun execute() {

        val shopId = UUID.fromString("beff0401-b879-49e4-803a-9268fca3e04f")
        val productId = UUID.fromString("a67b1851-bf67-4269-b397-55351628d198")
        val shopShow = ShopShowImpl(shopId, "Lululemon")
        val productOverview = ProductOverviewImpl(
            productId,
            "Hose",
            "Schwarz",
            shopId,
            "L",
            0,
            "HS0"
        )
        val stateCreate = StateCreateImpl(100f, LocalDateTime.now())

        val stateShow = StateShowImpl(
            UUID.fromString("db036557-8445-4274-b312-3f8227662c01"),
            100f,
            LocalDateTime.now()
        )


        every { shopServiceGateway.getShopServiceById(shopShow.name) } returns shopService

        every { shopService.track(productOverview) } returns stateCreate


        trackPrizesImpl.execute()

    }
}