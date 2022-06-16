package de.ma.infrastructure.configuration

import de.ma.pricetracker.application.tracker.manager.ProductManager
import org.jboss.logging.annotations.Producer
import javax.enterprise.context.Dependent
import javax.ws.rs.Produces

@Dependent
class BeansConfig {

    @Produces
    fun productManager() = ProductManager(

    )

}