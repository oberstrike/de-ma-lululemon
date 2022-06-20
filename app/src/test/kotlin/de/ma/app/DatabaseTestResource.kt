package de.ma.app

import com.github.dockerjava.api.model.ExposedPort
import com.github.dockerjava.api.model.HostConfig
import com.github.dockerjava.api.model.PortBinding
import com.github.dockerjava.api.model.Ports
import io.quarkus.test.common.QuarkusTestResourceLifecycleManager
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers

@Testcontainers
class DatabaseTestResource : QuarkusTestResourceLifecycleManager {

    @Container
    private lateinit var db: PostgreSQLContainer<*>


    override fun start(): Map<String, String> {
        val username = DatabaseConfig.username
        val password = DatabaseConfig.password

        db = PostgreSQLContainer<Nothing>(DatabaseConfig.container).apply {
            withDatabaseName(DatabaseConfig.database)
            withUsername(username)
            withPassword(password)
            withCreateContainerCmdModifier {
                HostConfig().withPortBindings(
                    PortBinding(
                        Ports.Binding.bindIp(DatabaseConfig.port),
                        ExposedPort(54333)
                    )
                )
            }
        }
        db.start()



        System.clearProperty("%test.xml.quarkus.hibernate-orm.dialect")
        //        System.setProperty("%test.xml.quarkus.datasource.jdbc.url", db.jdbcUrl)

        return mapOf(

         //   "quarkus.datasource.jdbc.url" to db.jdbcUrl,
            "quarkus.datasource.reactive.url" to db.jdbcUrl.replace("jdbc:", "vertx-reactive:"),
            "quarkus.datasource.db-kind" to "postgresql",
            "quarkus.datasource.username" to username,
            "quarkus.datasource.password" to password,
            "quarkus.hibernate-orm.database.generation" to "update",
            "quarkus.hibernate-orm.log.sql" to "true"
        )
    }

    override fun stop() {
        db.stop()
    }


}