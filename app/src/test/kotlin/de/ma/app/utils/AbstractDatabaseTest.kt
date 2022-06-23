package de.ma.app.utils

import io.quarkus.liquibase.LiquibaseFactory
import io.quarkus.test.common.QuarkusTestResource
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import javax.inject.Inject

@QuarkusTestResource(DatabaseTestResource::class)
abstract class AbstractDatabaseTest {

    @Inject
    lateinit var liquibaseFactory: LiquibaseFactory

    @AfterEach
    fun afterEach() {
        liquibaseFactory.createLiquibase().use { liquibase ->
            liquibase.dropAll()
            liquibase.validate()
            liquibase.update(liquibaseFactory.createContexts(), liquibaseFactory.createLabels())
        }
    }

}
